package com.example.notificationservice.listeners;

import com.example.notificationservice.model.Notification;
import com.example.notificationservice.request.SendNotificationRequest;
import com.example.notificationservice.service.NotificationService;
import com.example.notificationservice.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationListener {
    private final NotificationService notificationService;
    private final WebSocketNotificationService webSocketNotificationService;
    /**
     * Consume notification từ Kafka và push real-time qua WebSocket
     *
     * ORDERING:
     * - Kafka partition key phải là userId hoặc shopId để đảm bảo cùng partition
     * - max.in.flight.requests.per.connection = 1 đảm bảo processing tuần tự
     * - setConcurrency(1) trong factory đảm bảo 1 thread per partition
     */
    @KafkaListener(topics = "${kafka.topic.notification}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(final SendNotificationRequest request){
        log.info("NotificationListener.consume request: {}", request);
        Notification notification = notificationService.save(request);
        try{
            webSocketNotificationService.pushNotification(notification);
        } catch (Exception e){
            log.error("Failed to push notification via WebSocket: {}", e.getMessage(), e);
            // ko can throw exception, để tránh Kafka retry
        }
    }

//    @KafkaListener(topics = "${kafka.topic.notification}", groupId = "${spring.kafka.consumer.group-id}")
//    public void consume(final SendNotificationRequest request){
//        log.info("NotificationListener.consume request: {}", request);
//        notificationService.save(request);
//    }

}
