package com.example.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

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
    private String userId;
    private List<SizeDto> sizes;
}
