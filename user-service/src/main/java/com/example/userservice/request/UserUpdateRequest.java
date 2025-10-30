package com.example.userservice.request;

import com.example.userservice.model.UserDetails;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotBlank(message = "Id is required")
    private String id;
    private String email;
    private String username;
    private String password;
    private UserDetails userDetails;
}

