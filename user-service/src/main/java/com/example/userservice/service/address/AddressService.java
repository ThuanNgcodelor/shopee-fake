package com.example.userservice.service.address;

import com.example.userservice.model.Address;
import com.example.userservice.request.AddressCreateRequest;
import com.example.userservice.request.AddressUpdateRequest;

import java.util.List;

public interface AddressService {
    Address SaveAddress(AddressCreateRequest addressCreateRequest);
    Address UpdateAddress(AddressUpdateRequest addressUpdateRequest);
    Address GetAddressById(String addressId);
    void DeleteAddressById(String addressId);
    Address SetDefaultAddress(String addressId, String userId);
    Address GetDefaultAddress(String userId);
    List<Address> GetAllAddresses(String userId);
}
