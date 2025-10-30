package com.example.userservice.model;

import com.example.userservice.enums.Active;
import com.example.userservice.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.HashSet;
import java.util.List;

@Entity(name = "users")
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Column(unique = true,nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false, updatable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role primaryRole = Role.USER; // Role chính

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", columnDefinition = "VARCHAR(50)")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private Active active = Active.ACTIVE;

    @Embedded
    @Builder.Default
    private UserDetails userDetails = new UserDetails();
}
