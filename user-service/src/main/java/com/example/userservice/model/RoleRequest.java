package com.example.userservice.model;

import com.example.userservice.enums.RequestStatus;
import com.example.userservice.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "role_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRequest extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role requestedRole;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;
    
    @Column(columnDefinition = "TEXT")
    private String reason; // Lý do xin role
    
    @Column(columnDefinition = "TEXT")
    private String adminNote; // Ghi chú của admin
    
    private String reviewedBy; // Admin ID đã duyệt
    
    private LocalDateTime reviewedAt;
    
    @Column(columnDefinition = "TEXT")
    private String rejectionReason; // Lý do từ chối
}


