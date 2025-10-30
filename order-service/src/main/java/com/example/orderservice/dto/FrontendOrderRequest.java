package com.example.orderservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class FrontendOrderRequest {
    @NotEmpty(message = "Selected items cannot be empty")
    private List<SelectedItemDto> selectedItems;

    @NotNull(message = "Address ID is required")
    private String addressId;
}