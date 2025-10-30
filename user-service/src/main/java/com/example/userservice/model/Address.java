package com.example.userservice.model;

import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Address extends BaseEntity {
    public String userId;
    public String addressName;
    public String recipientName;
    public String recipientPhone;
    public String province;
    public String streetAddress;
    @Builder.Default
    public Boolean isDefault = false;
    
    // Thêm tọa độ địa lý
    private Double latitude;
    private Double longitude;
    private String city;
    private String district;
    
    // Method tính khoảng cách
    public double calculateDistance(double targetLat, double targetLng) {
        if (latitude == null || longitude == null) return Double.MAX_VALUE;
        return calculateHaversineDistance(latitude, longitude, targetLat, targetLng);
    }
    
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}