package com.example.userservice.repository;

import com.example.userservice.model.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity , String> {
    Optional<UserActivity> findByUserId(String userId);
}
