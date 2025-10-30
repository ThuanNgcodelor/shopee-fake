package com.example.userservice.request;

import lombok.Data;

@Data
public class AddressUpdateRequest {
    public String id;
    public String userId;
    public String addressName;
    public String recipientName;
    public String recipientPhone;
    public String province;
    public String streetAddress;
    public Boolean isDefault;
}
