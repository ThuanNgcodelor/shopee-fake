package com.example.userservice.model;

import com.example.userservice.enums.ActivityType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "shop_owner_id")
    private String shopOwnerId; // Optional: chỉ có khi activity liên quan đến shop owner
    
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;
    
    @Column(name = "activity_description")
    private String activityDescription;
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details; // JSON string chứa thông tin chi tiết
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "session_id")
    private String sessionId;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string cho các thông tin bổ sung
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    // Constructor cho các activity đơn giản
    public UserActivity(String userId, ActivityType activityType, String activityDescription) {
        this.userId = userId;
        this.activityType = activityType;
        this.activityDescription = activityDescription;
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor cho shop owner activities
    public UserActivity(String userId, String shopOwnerId, ActivityType activityType, String activityDescription) {
        this.userId = userId;
        this.shopOwnerId = shopOwnerId;
        this.activityType = activityType;
        this.activityDescription = activityDescription;
        this.createdAt = LocalDateTime.now();
    }
}
