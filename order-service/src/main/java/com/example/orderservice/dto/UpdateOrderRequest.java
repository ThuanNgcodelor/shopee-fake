package com.example.orderservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class UpdateOrderRequest {
    private String orderStatus;
    private String notes;
    private String shippingAddress;
    private String paymentMethod;
    private List<UpdateOrderItemRequest> orderItems;
}
