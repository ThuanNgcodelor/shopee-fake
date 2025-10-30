package com.example.stockservice.request.cart;

import lombok.Data;

@Data
public class AddCartItemRequest {
    private String cartId;
    private String productId;
    private String sizeId;
    private int quantity;
}
