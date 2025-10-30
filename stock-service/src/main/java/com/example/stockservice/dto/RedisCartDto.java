package com.example.stockservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedisCartDto {
    private String cartId;
    private String userId;
    private double totalAmount;
    private Map<String, RedisCartItemDto> items = new HashMap<>();

    public void calculateTotalAmount(){
        this.totalAmount = items.values().stream()
                .mapToDouble(RedisCartItemDto::getTotalPrice)
                .sum();
    }
}
