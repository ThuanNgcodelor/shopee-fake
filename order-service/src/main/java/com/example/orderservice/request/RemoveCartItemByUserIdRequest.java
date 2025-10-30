package com.example.orderservice.request;

import lombok.Data;

@Data
public class RemoveCartItemByUserIdRequest {
    private String userId;
    private String productId;
    private String sizeId;
}
