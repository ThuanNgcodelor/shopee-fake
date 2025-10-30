package com.example.orderservice.client;

import com.example.orderservice.dto.AddressDto;
import com.example.orderservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "user-service", path = "/v1/user")
public interface UserServiceClient {
    @GetMapping("/getUserById/{userId}")
    ResponseEntity<UserDto> getUserById(@PathVariable String userId);
    
    @GetMapping("/address/getAllAddresses")
    ResponseEntity<List<AddressDto>> getAllAddresses(@RequestHeader("Authorization") String authorization);
    
    @GetMapping("/address/getAddressById/{addressId}")
    ResponseEntity<AddressDto> getAddressById(@PathVariable String addressId);
}
