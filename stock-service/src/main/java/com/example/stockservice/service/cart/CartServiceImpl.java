package com.example.stockservice.service.cart;

import com.example.stockservice.dto.RedisCartDto;
import com.example.stockservice.model.Cart;
import com.example.stockservice.model.CartItem;
import com.example.stockservice.model.Product;
import com.example.stockservice.model.Size;
import com.example.stockservice.repository.SizeRepository;
import com.example.stockservice.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRedisService cartRedisService;
    private final ProductService productService;
    private final SizeRepository sizeRepository;

    @Override
    public Cart getCartByUserId(String userId) {
        RedisCartDto redisCart = cartRedisService.getCartByUserId(userId);
        if (redisCart == null) {
            return null;
        }
        return convertToModel(redisCart);
    }

    @Override
    public Cart initializeCart(String userId) {
        RedisCartDto redisCart = cartRedisService.getOrCreateCart(userId);
        return convertToModel(redisCart);
    }

    @Override
    public void clearCart(String userId) {
        cartRedisService.clearCart(userId);
    }

    @Override
    public Cart getUserCart(String userId, String cartId) {
        RedisCartDto redisCart = cartRedisService.getCartByUserId(userId);
        if (redisCart == null || !redisCart.getCartId().equals(cartId)) {
            throw new RuntimeException("Cart not found for user: " + userId + " and cartId: " + cartId);
        }
        return convertToModel(redisCart);
    }

    @Override
    public Cart getCartById(String cartId) {
        throw new RuntimeException("getCartById not supported in Redis implementation");
    }

    @Override
    public void clearCartByCartId(String cartId) {
        cartRedisService.deleteCartByCartId(cartId);
    }

    @Override
    public void removeCartItemsAndUpdateCart(String userId, List<String> productIds) {
        cartRedisService.removeCartItems(userId, productIds);
    }

    private Cart convertToModel(RedisCartDto redisCart) {
        if (redisCart == null) return null;

        Cart cart = Cart.builder()
                .userId(redisCart.getUserId())
                .totalAmount(redisCart.getTotalAmount())
                .items(new java.util.HashSet<>())
                .build();

        redisCart.getItems().forEach((key, item) -> {
            Product product = productService.getProductById(item.getProductId());
            
            Size size = null;
            if (item.getSizeId() != null && !item.getSizeId().isEmpty()) {
                size = sizeRepository.findById(item.getSizeId()).orElse(null);
            } else {
                System.out.println("Size not loaded - sizeId is null or empty");
            }

            CartItem cartItem = CartItem.builder()
                    .product(product)
                    .size(size)
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .totalPrice(item.getTotalPrice())
                    .build();
            
            // Set ID from Redis cartItemId
            if (item.getCartItemId() != null) {
                cartItem.setId(item.getCartItemId());
            }
            
            cart.getItems().add(cartItem);
        });

        return cart;
    }
}