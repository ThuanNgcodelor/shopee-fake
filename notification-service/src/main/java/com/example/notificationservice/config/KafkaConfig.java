package com.example.notificationservice.config;

import com.example.notificationservice.request.SendNotificationRequest;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topic.notification}")
    private String notificationTopic;

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public NewTopic topic() {
        return TopicBuilder.name(notificationTopic)
                .partitions(10)
                .replicas(1)
                .build();
    }

    @Bean
    public ConsumerFactory<String, SendNotificationRequest> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Enable auto-commit (hoặc manual commit nếu cần exactly-once)
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);

        // Cấu hình JsonDeserializer sử dụng kiểu cục bộ, không phụ thuộc vào type info trong message
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, SendNotificationRequest.class);
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SendNotificationRequest> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SendNotificationRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // CRITICAL: Đảm bảo ordering bằng cách process tuần tự
        // setConcurrency(1) = 1 thread cho tất cả partitions (an toàn nhất)
        // Hoặc setConcurrency(10) = 10 threads, mỗi thread 1 partition (nhanh hơn)
        factory.setConcurrency(1);

        return factory;
    }
}