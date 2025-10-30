package com.example.orderservice.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendNotificationRequest {
    private String userId;
    private String orderId;
    private String message;
}
