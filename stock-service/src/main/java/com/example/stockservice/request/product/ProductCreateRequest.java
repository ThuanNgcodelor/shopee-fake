package com.example.stockservice.request.product;

import com.example.stockservice.request.size.SizeRequest;
import lombok.Data;
import java.util.List;

@Data
public class ProductCreateRequest {
    private String name;
    private String description;
    private double price;
    private double originalPrice;
    private double discountPercent;
    private String categoryId;
    private String userId;
    private String imageId;
    private List<SizeRequest> sizes;
}
