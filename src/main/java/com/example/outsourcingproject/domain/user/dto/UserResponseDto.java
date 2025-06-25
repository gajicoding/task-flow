package com.example.outsourcingproject.domain.user.dto;

import com.example.outsourcingproject.domain.auth.entity.Auth;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseDto {
    private final Long id;
    private final String username;
    private final String email;
    private final String name;
    private final String role;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public UserResponseDto(Auth auth) {
        this.id = auth.getId();
        this.username = auth.getUsername();
        this.email = auth.getEmail();
        this.name = auth.getName();
        this.role = auth.getRole().toString();
        this.createdAt = auth.getCreatedAt();
        this.updatedAt = auth.getUpdatedAt();
    }
}
