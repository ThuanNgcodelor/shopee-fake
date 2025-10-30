package com.example.stockservice.request.size;

import lombok.Data;

@Data
public class SizeRequest {
    private String name; // e.g., "S", "M", "L", "XL"
    private String description;
    private int stock;
    private double priceModifier;
}

