package com.example.notificationservice.service;

import com.example.notificationservice.model.Notification;
import com.example.notificationservice.repository.NotificationRepository;
import com.example.notificationservice.request.SendNotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void save(SendNotificationRequest request){
        String userId = request.getUserId();
        String shopId = request.getShopId();
        
        if (userId != null && shopId != null && !userId.equals(shopId)) {
            shopId = userId;
        } else if (userId != null && shopId == null) {
            shopId = userId;
        } else if (userId == null && shopId != null) {
            userId = shopId;
        }
        
        boolean isShopOwnerNotification = request.getIsShopOwnerNotification() != null
            ? request.getIsShopOwnerNotification() 
            : false;
        
        var notification = Notification.builder()
                .userId(userId)
                .shopId(shopId)
                .orderId(request.getOrderId())
                .message(request.getMessage())
                .read(false)
                .shopOwnerNotification(isShopOwnerNotification)
                .build();
        
        notificationRepository.save(notification);
    }

    public List<Notification> getAllNotifications(String id){
        return notificationRepository.findAllByUserIdAndShopOwnerNotificationOrderByCreationTimestampDesc(id, false);
    }

    public List<Notification> getAllNotificationsByShopId(String shopId){
        return notificationRepository.findAllByShopIdAndShopOwnerNotificationOrderByCreationTimestampDesc(shopId, true);
    }

    public void markAsRead(String notificationId){
        var notification = notificationRepository.findById(notificationId).orElseThrow();
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void deleteNotification(String notificationId){
        notificationRepository.deleteById(notificationId);
    }

    public void deleteAllNotifications(String userId){
        var notifications = notificationRepository.findAllByUserIdAndShopOwnerNotificationOrderByCreationTimestampDesc(userId, false);
        notificationRepository.deleteAll(notifications);
    }

    public void markAllAsRead(String userId){
        var notifications = notificationRepository.findAllByUserIdAndShopOwnerNotificationOrderByCreationTimestampDesc(userId, false);
        for(var notification : notifications){
            notification.setRead(true);
        }
        notificationRepository.saveAll(notifications);
    }

    public void markAllAsReadByShopId(String shopId){
        var notifications = notificationRepository.findAllByShopIdAndShopOwnerNotificationOrderByCreationTimestampDesc(shopId, true);
        for(var notification : notifications){
            notification.setRead(true);
        }
        notificationRepository.saveAll(notifications);
    }

    public void deleteAllNotificationsByShopId(String shopId){
        var notifications = notificationRepository.findAllByShopIdAndShopOwnerNotificationOrderByCreationTimestampDesc(shopId, true);
        notificationRepository.deleteAll(notifications);
    }
}
