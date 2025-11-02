package com.example.userservice.service.user;

import com.example.userservice.client.FileStorageClient;
import com.example.userservice.client.StockServiceClient;
import com.example.userservice.dto.CartDto;
import com.example.userservice.dto.UserInformationDto;
import com.example.userservice.exception.NotFoundException;
import com.example.userservice.enums.Active;
import com.example.userservice.enums.Role;
import com.example.userservice.model.RoleRequest;
import com.example.userservice.model.User;
import com.example.userservice.model.UserDetails;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.request.RegisterRequest;
import com.example.userservice.request.UserUpdateRequest;
import com.example.userservice.service.role.RoleRequestService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service("userService")
public class UserServiceImpl implements UserService {
    private final FileStorageClient fileStorageClient;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final StockServiceClient stockServiceClient;
    private final RoleRequestService roleRequestService;

    @Override
    public CartDto getCart(HttpServletRequest request) {
        String author = request.getHeader("Authorization");
        ResponseEntity<CartDto> response = stockServiceClient.getCart(author);
        CartDto cartDto = response.getBody();

        if (cartDto == null) {
            CartDto emptyCart = new CartDto();
            emptyCart.setId(null);
            emptyCart.setUserId(null);
            emptyCart.setTotalAmount(0.0);
            emptyCart.setItems(java.util.Collections.emptyList());
            System.out.println("Returning empty cart from UserService");
            return emptyCart;
        }

        return cartDto;
    }

    @Override
    public User SaveUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmailIgnoreCase(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        Set<Role> initialRoles = new HashSet<>();
        initialRoles.add(Role.USER);

        User toSave = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .primaryRole(Role.USER)  
                .roles(initialRoles)     
                .active(Active.ACTIVE)
                .userDetails(new UserDetails())
                .build();
        return userRepository.save(toSave);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAllByActive(Active.ACTIVE);
    }

    @Override
    public User getUserById(String id) {
        return findUserById(id);
    }

    @Override
    public User getUserByEmail(String email) {
        return findUserByEmail(email);
    }

    @Override
    public User getUserByUsername(String username) {
        return findUserByUsername(username);
    }

    @Override
    public User updateUserById(UserUpdateRequest request, MultipartFile file) {
        User toUpdate = findUserById(request.getId());
        request.setUserDetails(updateUserDetails(toUpdate.getUserDetails(), request.getUserDetails(), file));
        modelMapper.map(request, toUpdate);
        return userRepository.save(toUpdate);
    }

    @Override
    public void deleteUserById(String id) {
        User toDelete = findUserById(id);
        toDelete.setActive(Active.INACTIVE);
        userRepository.save(toDelete);
    }

    @Override
    public User findUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public UserDetails updateUserDetails(UserDetails toUpdate, UserDetails request, MultipartFile file) {
        toUpdate = (toUpdate == null) ? new UserDetails() : toUpdate;

        if (file != null && !file.isEmpty()) {
            String profilePicture = fileStorageClient.uploadImageToFIleSystem(file).getBody();
            if (profilePicture != null) {
                // nếu muốn xóa ảnh cũ, cần kiểm tra null trước
                // fileStorageClient.deleteImageFromFileSystem(toUpdate.getImageUrl());
                toUpdate.setImageUrl(profilePicture);
            }
        }

        modelMapper.map(request, toUpdate);
        return toUpdate;
    }

    @Override
    public void updatePasswordByEmail(String email, String rawPassword) {
        User user = findUserByEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }

    @Override
    public List<RoleRequest> getUserRoleRequests(String userId) {
        return roleRequestService.getUserRequests(userId);
    }

    @Override
    public UserInformationDto convertUserToUserInformationDto(User user) {
        UserInformationDto dto = new UserInformationDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        
        UserDetails userDetails = user.getUserDetails();
        if (userDetails != null) {
            dto.setFirstName(userDetails.getFirstName());
            dto.setLastName(userDetails.getLastName());
            dto.setPhoneNumber(userDetails.getPhoneNumber());
            dto.setGender(userDetails.getGender() != null ? userDetails.getGender().toString() : null);
            dto.setAboutMe(userDetails.getAboutMe());
            dto.setBirthDate(String.valueOf(userDetails.getBirthDate()));
            dto.setImageUrl(userDetails.getImageUrl());
        }
        
        return dto;
    }
}
