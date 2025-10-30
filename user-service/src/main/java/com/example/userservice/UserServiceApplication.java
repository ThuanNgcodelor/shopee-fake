package com.example.userservice;

import com.example.userservice.enums.Active;
import com.example.userservice.enums.Role;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
@EnableFeignClients
public class UserServiceApplication implements CommandLineRunner {

    private final UserRepository userRepository;

    public UserServiceApplication(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Override
    public void run(String... args) {
        Set<Role> initialRoles = new HashSet<>();
        initialRoles.add(Role.ADMIN);
        final String pass = "$2a$10$mSqRLDmAXlUyN24pd5NoJ.w.7xnlzLwkfflhFhLcCkj8hM1mEzfnq";
        var admin = User.builder()
                .username("admin")
                .email("thuannguyen418@gmail.com")
                .password(pass)
                .primaryRole(Role.ADMIN)
                .roles(initialRoles)
                .active(Active.ACTIVE)
                .build();
        if (userRepository.findByUsername("admin").isEmpty()) userRepository.save(admin);
    }
}
