package com.example.stockservice.model;

import com.example.stockservice.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity(name = "products")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Product extends BaseEntity {
    private String name;
    private String description;
    private double price;
    private double originalPrice;
    private double discountPercent = 0;
    @Enumerated(EnumType.STRING)
    private ProductStatus status;
    private String imageId;
    
    private String userId;
    
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Size> sizes;
}
