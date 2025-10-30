package com.example.orderservice.dto;

import lombok.Data;
import jakarta.validation.constraints.Positive;

@Data
public class UpdateOrderItemRequest {
    private String id;
    private String productId;
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    private Double unitPrice;
    private String sizeId;
}
