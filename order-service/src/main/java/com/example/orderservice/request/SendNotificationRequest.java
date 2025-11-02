package com.example.orderservice.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendNotificationRequest {
    private String userId;
    private String shopId; // For shop owner notifications
    private String orderId;
    private String message;
    private Boolean isShopOwnerNotification; // true = notification for shop owner, false = notification for user
}
