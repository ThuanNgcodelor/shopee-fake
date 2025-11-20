package com.example.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationUpdateEvent {
    public enum UpdateType {
        MARKED_AS_READ,      // Notification đã được đánh dấu đã đọc
        DELETED,             // Notification đã bị xóa
        MARKED_ALL_AS_READ,  // Tất cả notifications đã được đánh dấu đã đọc
        DELETED_ALL          // Tất cả notifications đã bị xóa
    }

    private UpdateType updateType;
    private String notificationId;  // null nếu là bulk operation (deleteAll, markAllAsRead)
    private String userId;          // userId hoặc shopId
    private boolean isShopOwner;     // true nếu là shop owner notification
}

