package com.example.orderservice.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDto {
    private String id;
    private String userId;
    private Double totalPrice;
    private String orderStatus;
    private String notes;
    private String shippingAddress;
    private String paymentMethod;
    private LocalDateTime creationTimestamp;
    private LocalDateTime updateTimestamp;
    private List<OrderItemDto> orderItems;
}