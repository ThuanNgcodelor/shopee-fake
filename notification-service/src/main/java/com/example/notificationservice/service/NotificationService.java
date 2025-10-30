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
        var notification = Notification.builder()
                .userId(request.getUserId())
                .orderId(request.getOrderId())
                .message(request.getMessage())
                .build();
        notificationRepository.save(notification);
    }

    public List<Notification> getAllNotifications(String id){
        return notificationRepository.findAllByUserIdOrderByCreationTimestampDesc(id);
    }

}
