package com.example.userservice.service.address;

import com.example.userservice.client.UserServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.model.Address;
import com.example.userservice.repository.AddressRepository;
import com.example.userservice.request.AddressCreateRequest;
import com.example.userservice.request.AddressUpdateRequest;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final UserServiceClient userServiceClient;
    private final AddressRepository addressRepository;

    @Override
    public Address SaveAddress(AddressCreateRequest addressCreateRequest) {
        List<Address> existingAddresses = addressRepository.findAllByUserId(addressCreateRequest.getUserId());

        // Nếu chưa có địa chỉ nào => mặc định true
        boolean isDefault = existingAddresses.isEmpty();

        Address address = Address.builder()
                .userId(addressCreateRequest.getUserId())
                .addressName(addressCreateRequest.getAddressName())
                .recipientName(addressCreateRequest.getRecipientName())
                .recipientPhone(addressCreateRequest.getRecipientPhone())
                .province(addressCreateRequest.getProvince())
                .streetAddress(addressCreateRequest.getStreetAddress())
                .isDefault(isDefault)
                .build();

        return addressRepository.save(address);
    }


    @Override
    public List<Address> GetAllAddresses(String userId) {
        return addressRepository.findAllByUserId(userId);
    }

    @Override
    public Address GetDefaultAddress(String userId) {
        return addressRepository.findAllByUserId(userId).stream()
                .filter(Address::getIsDefault)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Address UpdateAddress(AddressUpdateRequest request) {
        Address address = findAddressById(request.getId());

        if (!address.getUserId().equals(request.getUserId())) {
            throw new NotFoundException("Address does not belong to this user");
        }

        address.setAddressName(request.getAddressName());
        address.setRecipientName(request.getRecipientName());
        address.setRecipientPhone(request.getRecipientPhone());
        address.setProvince(request.getProvince());
        address.setStreetAddress(request.getStreetAddress());

        // không động vào isDefault ở đây
        return addressRepository.save(address);
    }
    @Override
    public Address GetAddressById(String addressId) {
        return findAddressById(addressId);
    }

    @Override
    public void DeleteAddressById(String addressId) {
        Address address = findAddressById(addressId);
        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        String userId = address.getUserId();

        addressRepository.delete(address);

        // Nếu vừa xóa address default => chọn 1 address khác làm default
        if (wasDefault) {
            List<Address> remaining = addressRepository.findAllByUserId(userId);
            if (!remaining.isEmpty()) {
                Address newDefault = remaining.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }
    }

    @Override
    public Address SetDefaultAddress(String addressId, String userId) {
        Address address = findAddressById(addressId);
        UserDto user = getUserById(userId);
        if(!user.getId().equals(address.getUserId())) {
            return null;
        }

        List<Address> allAddresses = addressRepository.findAllByUserId(user.getId());
        for(Address a : allAddresses) {
            if(a.getIsDefault()){
                a.setIsDefault(false);
            }
        }
        address.setIsDefault(true);
        allAddresses.add(address);
        addressRepository.saveAll(allAddresses);
        return address;
    }

    protected UserDto getUserById(String userId) {
        return Optional.ofNullable(userServiceClient.getUserById(userId).getBody())
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
    }

    protected Address findAddressById(String addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Address not found with id: " + addressId));
    }

}
