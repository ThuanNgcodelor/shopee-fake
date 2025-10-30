package com.example.stockservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity(name = "sizes")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Size extends BaseEntity {
    private String name; // e.g., "S", "M", "L", "XL"
    private String description; // e.g., "Small"
    private int stock;
    private double priceModifier;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @JsonIgnore
    @OneToMany(mappedBy = "size", cascade = CascadeType.ALL)
    private List<CartItem> cartItems;
}

