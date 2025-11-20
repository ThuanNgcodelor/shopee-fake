package com.example.notificationservice.service;

import com.example.notificationservice.model.Notification;
import com.example.notificationservice.dto.NotificationUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Push notification to specific user via WebSocket
     * Destination: /topic/user/{userId}
     */
    public void pushNotificationToUser(String userId, Notification notification){
        String destination = "/topic/user/" + userId;
        log.info("Pushing notification to user {} via WebSocket: {}", userId, notification.getId());
        messagingTemplate.convertAndSend(destination, notification);
    }
    
    /**
     * Push notification to shop owner via WebSocket
     * Destination: /topic/shop/{shopId}
     */
    public void pushNotificationToShop(String shopId, Notification notification){
        String destination = "/topic/shop/" + shopId;
        log.info("Pushing notification to shop {} via WebSocket: {}", shopId, notification.getId());
        messagingTemplate.convertAndSend(destination, notification);
    }

    /**
     * Push notification based on notification type
     */
    public void pushNotification(Notification notification) {
        if (notification.isShopOwnerNotification()) {
            pushNotificationToShop(notification.getShopId(), notification);
        } else {
            pushNotificationToUser(notification.getUserId(), notification);
        }
    }

    /**
     * Broadcast notification update event (mark as read, delete, etc.)
     * Destination: /topic/user/{userId}/updates hoặc /topic/shop/{shopId}/updates
     */
    public void broadcastUpdate(String userId, String shopId, boolean isShopOwner, NotificationUpdateEvent updateEvent) {
        String destination = isShopOwner 
            ? "/topic/shop/" + shopId + "/updates"
            : "/topic/user/" + userId + "/updates";
        
        log.info("Broadcasting update event {} to {} via WebSocket", updateEvent.getUpdateType(), destination);
        messagingTemplate.convertAndSend(destination, updateEvent);
    }
}
