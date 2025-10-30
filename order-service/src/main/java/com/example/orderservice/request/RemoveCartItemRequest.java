package com.example.orderservice.request;

import lombok.Data;

import java.util.List;

@Data
public class RemoveCartItemRequest {
    public String cartId;
    public List<String> productIds;
}
