package com.example.stockservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RedisCartItemDto {
    private String cartItemId;
    private String productId;
    private String sizeId;
    private String sizeName;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private String productName;
    private String imageId;
}
