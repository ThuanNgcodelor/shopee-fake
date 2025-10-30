package com.example.stockservice.request.cart;

import lombok.Data;

@Data
public class UpdateCartItemRequest {
    private String userId;
    private String productId;
    private String sizeId;
    private int quantity;
}
