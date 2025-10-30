package com.example.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopOwnerDto {
    private String userId;
    private String shopName;
    private String ownerName;
    private String address;
    private Boolean verified;
    private Integer totalRatings;
    private Integer followersCount;
    private Integer followingCount;
    private String imageUrl;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
