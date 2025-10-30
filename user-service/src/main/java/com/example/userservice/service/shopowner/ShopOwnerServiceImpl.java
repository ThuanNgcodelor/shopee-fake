package com.example.userservice.service.shopowner;

import com.example.userservice.client.FileStorageClient;
import com.example.userservice.model.ShopOwner;
import com.example.userservice.repository.ShopOwnerRepository;
import com.example.userservice.request.UpdateShopOwnerRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service("shopOwnerService")
public class ShopOwnerServiceImpl implements ShopOwnerService {
    private final ShopOwnerRepository shopOwnerRepository;
    private final ModelMapper modelMapper;
    private final FileStorageClient fileStorageClient;

    @Override
    public ShopOwner updateShopOwner(UpdateShopOwnerRequest request, MultipartFile file) {
        ShopOwner toUpdate = shopOwnerRepository.getReferenceById(request.getUserId());
        
        // Update fields from request
        if (request.getShopName() != null) {
            toUpdate.setShopName(request.getShopName());
        }
        if (request.getOwnerName() != null) {
            toUpdate.setOwnerName(request.getOwnerName());
        }
        if (request.getEmail() != null) {
            toUpdate.setEmail(request.getEmail());
        }
        if (request.getAddress() != null) {
            toUpdate.setAddress(request.getAddress());
        }
        
        // Upload image if provided
        if(file != null && !file.isEmpty()) {
            String imageId = fileStorageClient.uploadImageToFIleSystem(file).getBody();
            if(imageId != null) {
                toUpdate.setImageUrl(imageId);
            }
        }
        
        return shopOwnerRepository.save(toUpdate);
    }
    
    @Override
    public ShopOwner getShopOwnerByUserId(String userId) {
        return shopOwnerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Shop owner not found with userId: " + userId));
    }
}
