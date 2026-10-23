package com.example.userservice.dto;

import com.example.userservice.entity.AppUser;
import com.example.userservice.entity.UserStatus;

import java.time.LocalDateTime;

public record UserResponse(Long id, String fullName, String email, String phoneNumber,
                           UserStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getPhoneNumber(),
                user.getStatus(), user.getCreatedAt(), user.getUpdatedAt());
    }
}
