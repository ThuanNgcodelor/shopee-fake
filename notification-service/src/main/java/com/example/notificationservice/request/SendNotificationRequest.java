package com.example.notificationservice.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SendNotificationRequest {
    private String userId;
    private String orderId;
    private String message;
}