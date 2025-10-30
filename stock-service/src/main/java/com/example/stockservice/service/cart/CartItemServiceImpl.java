package com.example.stockservice.service.cart;

import com.example.stockservice.dto.RedisCartItemDto;
import com.example.stockservice.model.CartItem;
import com.example.stockservice.model.Product;
import com.example.stockservice.model.Size;
import com.example.stockservice.repository.SizeRepository;
import com.example.stockservice.request.cart.AddCartItemRequest;
import com.example.stockservice.request.cart.UpdateCartItemRequest;
import com.example.stockservice.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartItemServiceImpl implements CartItemService {

    private final CartRedisService cartRedisService;
    private final ProductService productService;
    private final SizeRepository sizeRepository;

    @Override
    public CartItem addCartItem(AddCartItemRequest request, String userId) {
        RedisCartItemDto redisItem = cartRedisService.addItemToCart(
                userId,
                request.getProductId(),
                request.getSizeId(),
                request.getQuantity()
        );
        return convertToModel(redisItem);
    }

    @Override
    public CartItem updateCartItem(UpdateCartItemRequest request) {
        try {
            RedisCartItemDto redisItem = cartRedisService.updateCartItem(
                    request.getUserId(),
                    request.getProductId(),
                    request.getSizeId(),
                    request.getQuantity()
            );
            return convertToModel(redisItem);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void removeCartItem(String userId, String productId, String sizeId) {
        cartRedisService.removeCartItem(userId, productId, sizeId);
    }
    
    @Override
    public void removeCartItemByCartItemId(String userId, String cartItemId) {
        cartRedisService.removeCartItemByCartItemId(userId, cartItemId);
    }

    private CartItem convertToModel(RedisCartItemDto redisItem) {
        if (redisItem == null) return null;

        Product product = productService.getProductById(redisItem.getProductId());

        Size size = null;
        if (redisItem.getSizeId() != null && !redisItem.getSizeId().isEmpty()) {
            size = sizeRepository.findById(redisItem.getSizeId()).orElse(null);
        }

        CartItem cartItem = CartItem.builder()
                .product(product)
                .size(size)
                .quantity(redisItem.getQuantity())
                .unitPrice(redisItem.getUnitPrice())
                .totalPrice(redisItem.getTotalPrice())
                .build();

        if (redisItem.getCartItemId() != null) {
            cartItem.setId(redisItem.getCartItemId());
        }

        return cartItem;
    }
}