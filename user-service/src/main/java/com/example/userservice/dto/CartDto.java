package com.example.userservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class CartDto {
    private String id;
    private String userId;
    private double totalAmount;
    private List<CartItemDto> items;
}
