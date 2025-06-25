package com.example.outsourcingproject.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class SigninResponseDto {

    private final Long userId;
    private final String token;
    private final String refreshToken;

    public SigninResponseDto(Long userId, String token, String refreshToken) {
        this.userId = userId;
        this.token = token;
        this.refreshToken = refreshToken;
    }
}