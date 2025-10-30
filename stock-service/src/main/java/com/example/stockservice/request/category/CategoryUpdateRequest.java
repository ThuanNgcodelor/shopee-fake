package com.example.stockservice.request.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryUpdateRequest {
    @NotBlank(message = "Category id cannot be blank")
    private String id;
    private String name;
    private String description;
}
