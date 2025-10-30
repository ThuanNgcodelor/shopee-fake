package com.example.stockservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "cart_items")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CartItem extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @ManyToOne
    @JoinColumn(name = "size_id")
    private Size size;

    private int quantity;
    private double unitPrice;
    private double totalPrice;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
    
    public void setTotalPrice() {
        this.totalPrice = this.unitPrice * this.quantity;
    }
}
