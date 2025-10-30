package com.example.stockservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDto {
    private String id;
    private String name;
    private String description;
    private double price;
    private double originalPrice;
    private double discountPercent;
    private String imageId;
    private String status;
    private String categoryName;
    private String categoryId;
    private String userId;
    private List<SizeDto> sizes;
    private Integer totalStock;
    private LocalDateTime createdAt;
}
