package com.example.userservice.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleRequestRequest {
    @NotBlank(message = "Role is required")
    private String role;

    private String reason;
}