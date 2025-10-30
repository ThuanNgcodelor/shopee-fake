package com.example.userservice.controller;

import com.example.userservice.dto.AddressDto;
import com.example.userservice.jwt.JwtUtil;
import com.example.userservice.request.AddressCreateRequest;
import com.example.userservice.request.AddressUpdateRequest;
import com.example.userservice.service.address.AddressService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user/address")
public class AddressController {
    private final AddressService addressService;
    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;

    @PostMapping("/save")
    ResponseEntity<AddressDto> saveAddress(@RequestBody AddressCreateRequest addressCreateRequest, HttpServletRequest request){
        String userId = jwtUtil.ExtractUserId(request);
        addressCreateRequest.setUserId(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelMapper.map(addressService.SaveAddress(addressCreateRequest), AddressDto.class));
    }

    @GetMapping("/getAddressById/{addressId}")
    ResponseEntity<AddressDto> getAddressById(@PathVariable("addressId") String addressId){
        return ResponseEntity.ok(modelMapper.map(addressService.GetAddressById(addressId), AddressDto.class));
    }

    @DeleteMapping("/deleteAddressById/{addressId}")
    ResponseEntity<Void> deleteAddressById(@PathVariable("addressId") String addressId){
        addressService.DeleteAddressById(addressId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getAllAddresses")
    ResponseEntity<List<AddressDto>> getAllAddresses(HttpServletRequest request){
        String userId = jwtUtil.ExtractUserId(request);
        List<AddressDto> addressDto = addressService.GetAllAddresses(userId).stream()
                .map(address -> modelMapper.map(address, AddressDto.class))
                .toList();
        return ResponseEntity.ok(addressDto);
    }

    @PutMapping("/update")
    ResponseEntity<AddressDto> updateAddress(@RequestBody AddressUpdateRequest request, HttpServletRequest httpRequest) {
        String userId = jwtUtil.ExtractUserId(httpRequest);
        request.setUserId(userId);
        return ResponseEntity.ok(
                modelMapper.map(addressService.UpdateAddress(request), AddressDto.class)
        );
    }

    @PutMapping("/setDefaultAddress/{addressId}")
    ResponseEntity<AddressDto> setDefaultAddress(@PathVariable String addressId, HttpServletRequest request) {
        String userId = jwtUtil.ExtractUserId(request);
        return ResponseEntity.ok(
                modelMapper.map(addressService.SetDefaultAddress(addressId, userId), AddressDto.class)
        );
    }

}
