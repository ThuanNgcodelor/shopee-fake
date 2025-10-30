package com.example.userservice.dto;

import com.example.userservice.enums.ActivityType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivityDto {
    private String id;
    private String userId;
    private String shopOwnerId;
    private ActivityType activityType;
    private String activityDescription;
    private String details;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private String metadata;
    private LocalDateTime createdAt;
    
    // Additional fields for display
    private String activityTypeDisplayName;
    private String activityTypeDescription;
    private String timeAgo; // e.g., "2 hours ago", "1 day ago"
}
