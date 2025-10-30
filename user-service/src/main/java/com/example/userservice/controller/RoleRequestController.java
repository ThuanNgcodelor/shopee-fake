package com.example.userservice.controller;

import com.example.userservice.enums.Role;
import com.example.userservice.jwt.JwtUtil;
import com.example.userservice.model.RoleRequest;
import com.example.userservice.request.RoleRequestRequest;
import com.example.userservice.request.RoleRequestResponse;
import com.example.userservice.service.role.RoleRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/user/role-requests")
@RequiredArgsConstructor
public class RoleRequestController {
    
    private final RoleRequestService roleRequestService;
    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;

    @GetMapping("/{requestId}")
    ResponseEntity<RoleRequestResponse> getRequestById(@PathVariable String requestId){
        return ResponseEntity.ok(modelMapper.map(roleRequestService.getRoleRequestById(requestId),RoleRequestResponse.class));
    }

    @PostMapping
    public ResponseEntity<RoleRequestResponse> createRoleRequest(
            @Valid @RequestBody RoleRequestRequest request,
            HttpServletRequest httpRequest) {

        System.out.println("Received role request - role: " + request.getRole() + ", reason: " + request.getReason());
        
        String userId = jwtUtil.ExtractUserId(httpRequest);
        System.out.println("Extracted userId: " + userId);
        
        // 验证 role 是否为空
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }
        
        Role requestedRole;
        try {
            requestedRole = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }
        
        RoleRequest created = roleRequestService.createRoleRequest(
                userId,
                requestedRole,
                request.getReason()
        );

        RoleRequestResponse response = RoleRequestResponse.builder()
                .id(created.getId())
                .userId(created.getUser().getId())
                .requestedRole(created.getRequestedRole().name())
                .reason(created.getReason())
                .status(created.getStatus().name())
                .build();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/pending")
    public ResponseEntity<List<RoleRequestResponse>> getPendingRequests() {
        List<RoleRequestResponse> responses = roleRequestService.getPendingRequests().stream()
                .map(rr -> RoleRequestResponse.builder()
                        .id(rr.getId())
                        .userId(rr.getUser() != null ? rr.getUser().getId() : null)
                        .requestedRole(rr.getRequestedRole().name())
                        .reason(rr.getReason())
                        .status(rr.getStatus().name())
                        .creationTimestamp(rr.getCreationTimestamp())
                        .adminNote(rr.getAdminNote())
                        .username(rr.getUser() != null ? rr.getUser().getUsername() : null)
                        .build())
                .toList();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/user")
    public ResponseEntity<List<RoleRequestResponse>> getUserRequests(HttpServletRequest request) {
        String userId = jwtUtil.ExtractUserId(request);
        List<RoleRequestResponse> responses = roleRequestService.getUserRequests(userId).stream()
                .map(rr -> RoleRequestResponse.builder()
                        .id(rr.getId())
                        .userId(rr.getUser() != null ? rr.getUser().getId() : null)
                        .requestedRole(rr.getRequestedRole().name())
                        .reason(rr.getReason())
                        .status(rr.getStatus().name())
                        .creationTimestamp(rr.getCreationTimestamp())
                        .adminNote(rr.getAdminNote())
                        .username(rr.getUser() != null ? rr.getUser().getUsername() : null)
                        .build())
                .toList();
        return ResponseEntity.ok(responses);
    }

    //{{baseURL}}/v1/user/role-requests/ee24db1d-ae50-4894-9a94-9648d2baf4e2/approve?adminNote=vcl
    // ee24db1d-ae50-4894-9a94-9648d2baf4e2 laf ID cua roleRequest
    // adminNote=vcl ???
    // nhớ kẹp token admin
    @PostMapping("/{requestId}/approve")
    public ResponseEntity<RoleRequestResponse> approveRequest(
            @PathVariable String requestId,
            HttpServletRequest request,
            @RequestParam(required = false) String adminNote) {

        String adminId = jwtUtil.ExtractUserId(request);
        RoleRequest roleRequest = roleRequestService.approveRequest(requestId, adminId, adminNote);

        RoleRequestResponse response = RoleRequestResponse.builder()
                .id(roleRequest.getId())
                .userId(roleRequest.getUser() != null ? roleRequest.getUser().getId() : null)
                .requestedRole(roleRequest.getRequestedRole().name())
                .reason(roleRequest.getReason())
                .status(roleRequest.getStatus().name())
                .creationTimestamp(roleRequest.getCreationTimestamp())
                .adminNote(roleRequest.getAdminNote())
                .username(roleRequest.getUser() != null ? roleRequest.getUser().getUsername() : null)
                .build();

        return ResponseEntity.ok(response);
    }

    //{{baseURL}}/v1/user/role-requests/ee24db1d-ae50-4894-9a94-9648d2baf4e2/reject?rejectionReason=vcl
    // ee24db1d-ae50-4894-9a94-9648d2baf4e2 laf ID cua roleRequest
    // adminNote=vcl ???
    // nhớ kẹp token admin
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<RoleRequestResponse> rejectRequest(
            @PathVariable String requestId,
            HttpServletRequest request,
            @RequestParam String rejectionReason) {
        String adminId = jwtUtil.ExtractUserId(request);
        RoleRequest roleRequest = roleRequestService.rejectRequest(requestId, adminId, rejectionReason);

        RoleRequestResponse response = RoleRequestResponse.builder()
                .id(roleRequest.getId())
                .userId(roleRequest.getUser() != null ? roleRequest.getUser().getId() : null)
                .requestedRole(roleRequest.getRequestedRole().name())
                .reason(roleRequest.getReason())
                .status(roleRequest.getStatus().name())
                .creationTimestamp(roleRequest.getCreationTimestamp())
                .adminNote(roleRequest.getAdminNote())
                .username(roleRequest.getUser() != null ? roleRequest.getUser().getUsername() : null)
                .build();

        return ResponseEntity.ok(response);
    }
}