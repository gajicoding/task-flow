package com.example.outsourcingproject.domain.user.enums;

import com.example.outsourcingproject.domain.user.exception.InvalidUserRoleException;

import java.util.Arrays;

public enum UserRole {
    ADMIN, USER;

    public static UserRole of(String role) {
        return Arrays.stream(UserRole.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new InvalidUserRoleException("유효하지 않은 UserRole 입니다."));  // 수정
    }
}