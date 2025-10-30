package com.example.userservice.dto;

import lombok.Data;

@Data
public class AddressDto {
    public String userId;
    private String id;
    private String addressName;
    private String recipientName;
    private String recipientPhone;
    private String province;
    private String streetAddress;
    private Boolean isDefault;
}
