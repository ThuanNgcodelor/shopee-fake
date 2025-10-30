package com.example.stockservice.request.cart;

import lombok.Data;

import java.util.List;

@Data
public class RemoveCartItemRequest {
    private String cartId;
    private List<String> productIds;
}