package com.example.userservice.service.user;

import com.example.userservice.dto.CartDto;
import com.example.userservice.dto.UserInformationDto;
import com.example.userservice.model.User;
import com.example.userservice.model.UserDetails;
import com.example.userservice.request.RegisterRequest;
import com.example.userservice.request.UserUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    User SaveUser(RegisterRequest registerRequest);
    List<User> getAllUsers();
    User getUserById(String id);
    User getUserByEmail(String email);
    User getUserByUsername(String username);
    User updateUserById(UserUpdateRequest request, MultipartFile file);
    void deleteUserById(String id);
    User findUserById(String id);
    User findUserByUsername(String username);
    User findUserByEmail(String email);
    UserDetails updateUserDetails(UserDetails toUpdate,UserDetails request, MultipartFile file);
    CartDto getCart(HttpServletRequest request);
    void updatePasswordByEmail(String email, String rawPassword);
    List<com.example.userservice.model.RoleRequest> getUserRoleRequests(String userId);
    UserInformationDto convertUserToUserInformationDto(User user);
    

}
