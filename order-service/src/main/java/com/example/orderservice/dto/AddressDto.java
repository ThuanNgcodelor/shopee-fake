package com.example.orderservice.dto;

import lombok.Data;

@Data
public class AddressDto {
    private String userId;
    private String addressId;
    private String addressName;
    private String recipientName;
    private String recipientPhone;
    private String province;
    private String streetAddress;
    private Boolean isDefault;
}
