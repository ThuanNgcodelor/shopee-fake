package com.example.stockservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SizeDto {
    private String id;
    private String name;
    private String description;
    private int stock;
    private double priceModifier;
}
