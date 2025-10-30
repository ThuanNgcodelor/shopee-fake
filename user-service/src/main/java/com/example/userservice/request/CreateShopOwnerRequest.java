package com.example.userservice.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShopOwnerRequest {
    @NotBlank(message = "Shop name is required")
    @Size(min = 2, max = 100, message = "Shop name must be between 2 and 100 characters")
    private String shopName;
    
    @NotBlank(message = "Owner name is required")
    @Size(min = 2, max = 50, message = "Owner name must be between 2 and 50 characters")
    private String ownerName;
    
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
}
