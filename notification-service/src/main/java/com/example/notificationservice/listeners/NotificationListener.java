package com.example.notificationservice.listeners;

import com.example.notificationservice.request.SendNotificationRequest;
import com.example.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationListener {
    private final NotificationService notificationService;

    @KafkaListener(topics = "${kafka.topic.notification}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(final SendNotificationRequest request){
        log.info("NotificationListener.consume request: {}", request);
        notificationService.save(request);
    }

}
