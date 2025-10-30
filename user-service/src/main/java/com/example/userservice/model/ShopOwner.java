package com.example.userservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shop_owners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopOwner {
    @Id
    @Column(name = "user_id")
    private String userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "shop_name", nullable = false)
    private String shopName;
    
    @Column(name = "owner_name", nullable = false)
    private String ownerName;
    
    @Column(name = "address")
    private String address;

    @Builder.Default
    @Column(name = "verified")
    private Boolean verified = false;
    
    @Builder.Default
    @Column(name = "total_ratings")
    private Integer totalRatings = 0;
    
    @Builder.Default
    @Column(name = "followers_count")
    private Integer followersCount = 0;
    
    @Builder.Default
    @Column(name = "following_count")
    private Integer followingCount = 0;

    @Column(name = "profile_image_url")
    private String imageUrl;

    @Column(name = "email")
    private String email;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
