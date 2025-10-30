package com.example.userservice.repository;

import com.example.userservice.enums.Role;
import com.example.userservice.model.RoleRequest;
import com.example.userservice.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRequestRepository extends JpaRepository<RoleRequest, String> {
    
    List<RoleRequest> findByStatusOrderByCreationTimestampDesc(RequestStatus status);
    
    List<RoleRequest> findByUserIdOrderByCreationTimestampDesc(String userId);
    
    Optional<RoleRequest> findByUserIdAndStatus(String userId, RequestStatus status);
    
    Optional<RoleRequest> findByUserIdAndRequestedRoleAndStatus(String userId, Role requestedRole, RequestStatus status);
    
    long countByStatus(RequestStatus status);
}
