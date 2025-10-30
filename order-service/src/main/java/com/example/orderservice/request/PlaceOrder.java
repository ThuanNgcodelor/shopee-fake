package com.example.orderservice.request;

import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.model.OrderItem;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PlaceOrder {
    private String orderId;
    private String userId;
    private double totalPrice;
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItem> orderItems;
}
