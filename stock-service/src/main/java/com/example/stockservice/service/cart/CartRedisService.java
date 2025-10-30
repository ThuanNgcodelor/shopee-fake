package com.example.stockservice.service.cart;


import com.example.stockservice.dto.RedisCartDto;
import com.example.stockservice.dto.RedisCartItemDto;
import com.example.stockservice.model.Product;
import com.example.stockservice.model.Size;
import com.example.stockservice.repository.SizeRepository;
import com.example.stockservice.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartRedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductService productService;
    private final SizeRepository sizeRepository;

    private static final String CART_KEY_PREFIX = "cart:user:";
    private static final long CART_TTL_MINUTES = 30;

    // Lấy giỏ hàng từ Redis, nếu không có thì tạo mới
    public RedisCartDto getOrCreateCart(String userId) {
        String key = CART_KEY_PREFIX + userId;
        RedisCartDto cart = (RedisCartDto) redisTemplate.opsForValue().get(key);
        if (cart == null) {
            cart = RedisCartDto.builder()
                    .cartId(UUID.randomUUID().toString())
                    .userId(userId)
                    .totalAmount(0.0)
                    .items(new HashMap<>())
                    .build();
            saveCart(userId, cart);
        }
        return cart;
    }

    // Lưu giỏ hàng vào Redis với TTL
    private void saveCart(String userId, RedisCartDto cart) {
        String key = CART_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, cart, CART_TTL_MINUTES, TimeUnit.MINUTES);
    }

    // Lấy giỏ hàng từ Redis theo userId
    public RedisCartDto getCartByUserId(String userId) {
        String key = CART_KEY_PREFIX + userId;
        return (RedisCartDto) redisTemplate.opsForValue().get(key);
    }

    // Thêm product vào giỏ hàng
    public RedisCartItemDto addItemToCart(String userId, String productId, String sizeId, int quantity) {
        RedisCartDto cart = getOrCreateCart(userId); // Lấy giỏ hàng hoặc tạo mới nếu chưa có
        Product product = productService.getProductById(productId);
        if (product == null) throw new RuntimeException("Product not found");
        String itemKey = productId + (sizeId != null && !sizeId.isEmpty() ? ":" + sizeId : ""); // Tạo key duy nhất cho item dựa trên productId và sizeId

        // Calculate price with size modifier
        double unitPrice = product.getPrice();
        String sizeName = null;
        if (sizeId != null && !sizeId.isEmpty()) {
            Size size = sizeRepository.findById(sizeId)
                    .orElseThrow(() -> new RuntimeException("Size not found with id: " + sizeId));
            unitPrice = product.getPrice() + size.getPriceModifier();
            sizeName = size.getName();
        }

        RedisCartItemDto cartItem = cart.getItems().getOrDefault(itemKey, null); // Kiểm tra item đã có trong giỏ hàng chưa
        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + quantity); // Cập nhật số lượng nếu đã có
        } else {
            cartItem = RedisCartItemDto.builder() // Tạo mới item nếu chưa có
                    .cartItemId(UUID.randomUUID().toString())
                    .productId(productId)
                    .sizeId(sizeId)
                    .sizeName(sizeName)
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .productName(product.getName())
                    .imageId(product.getImageId())
                    .build();
        }

        cartItem.setTotalPrice(cartItem.getQuantity() * cartItem.getUnitPrice()); // Cập nhật tổng giá tiền của item
        cart.getItems().put(itemKey, cartItem); // Thêm hoặc cập nhật item trong giỏ hàng

        cart.calculateTotalAmount();
        saveCart(userId, cart); // Lưu giỏ hàng cập nhật vào Redis
        return cartItem;
    }

    // Cập nhật số lượng của một item trong giỏ hàng
    public RedisCartItemDto updateCartItem(String userId, String productId, String sizeId, int quantity) {
        RedisCartDto cart = getCartByUserId(userId); // Lấy giỏ hàng từ Redis
        if (cart == null) {
            throw new RuntimeException("Cart not found for user: " + userId);
        }

        String itemKey = productId + (sizeId != null && !sizeId.isEmpty() ? ":" + sizeId : "");
        RedisCartItemDto item = cart.getItems().get(itemKey);
        
        if (item == null && (sizeId == null || sizeId.isEmpty())) {
            item = cart.getItems().get(productId);
        }
        
        if (item == null) {
            throw new RuntimeException("Item not found in cart for key: " + itemKey);
        }

        item.setQuantity(quantity); // Cập nhật số lượng
        item.setTotalPrice(item.getUnitPrice() * quantity); // Cập nhật tổng giá tiền của item

        cart.calculateTotalAmount();
        saveCart(userId, cart);

        return item;
    }

    // Xóa một item khỏi giỏ hàng
    public void removeCartItem(String userId, String productId, String sizeId) {
        log.info("[REDIS] Starting removeCartItem - userId: {}, productId: {}, sizeId: {}", 
            userId, productId, sizeId);
        
        RedisCartDto cart = getCartByUserId(userId);
        if (cart == null) {
            log.error("[REDIS] Cart not found for userId: {}", userId);
            throw new RuntimeException("Cart not found for user: " + userId);
        }

        String itemKey = productId + (sizeId != null && !sizeId.isEmpty() ? ":" + sizeId : "");
        log.info("[REDIS] Generated itemKey: {}, Available keys in cart: {}", itemKey, cart.getItems().keySet());
        
        RedisCartItemDto removed = cart.getItems().remove(itemKey);
        if (removed == null) {
            log.warn("[REDIS] Item not found with key: {}, Available keys: {}", itemKey, cart.getItems().keySet());
        } else {
            log.info("[REDIS] Successfully removed item: {}, quantity: {}, totalPrice: {}", 
                itemKey, removed.getQuantity(), removed.getTotalPrice());
        }
        
        cart.calculateTotalAmount();
        log.info("[REDIS] Updated cart totalAmount: {}", cart.getTotalAmount());
        saveCart(userId, cart);
        
        log.info("[REDIS] Cart saved to Redis after removal");
    }
    
    // Xóa một item khỏi giỏ hàng theo cartItemId
    public void removeCartItemByCartItemId(String userId, String cartItemId) {
        RedisCartDto cart = getCartByUserId(userId);
        if (cart == null) {
            throw new RuntimeException("Cart not found for user: " + userId);
        }

        log.info("Removing cartItemId: {} from userId: {}", cartItemId, userId);
        log.info("Current cart items before removal: {}", cart.getItems().keySet());
        
        // Find and remove the item by cartItemId
        boolean removed = cart.getItems().entrySet().removeIf(entry -> 
            entry.getValue().getCartItemId().equals(cartItemId)
        );
        
        if (!removed) {
            log.warn("CartItem with ID {} not found in cart", cartItemId);
            throw new RuntimeException("CartItem with ID " + cartItemId + " not found");
        }
        
        log.info("Cart item removed successfully. Remaining items: {}", cart.getItems().keySet());
        
        cart.calculateTotalAmount();
        saveCart(userId, cart);
    }

    // Xóa tất cả items của nhiều sản phẩm khỏi giỏ hàng
    public void removeCartItems(String userId, List<String> productIds) {
        RedisCartDto cart = getCartByUserId(userId);
        if (cart == null) {
            throw new RuntimeException("Cart not found for user: " + userId);
        }

        for (String productId : productIds) {
            cart.getItems().entrySet().removeIf(entry -> entry.getValue().getProductId().equals(productId));
        }
        cart.calculateTotalAmount();
        saveCart(userId, cart);
    }

    public void clearCart(String userId) {
        String key = CART_KEY_PREFIX + userId;
        redisTemplate.delete(key);
    }

    public void deleteCartByCartId(String cartId) {
        redisTemplate.delete(cartId);
    }
}