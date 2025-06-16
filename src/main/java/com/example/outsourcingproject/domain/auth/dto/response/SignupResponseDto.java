package com.example.outsourcingproject.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class SignupResponseDto {

    private final String token;
    private final String refreshToken;

    public SignupResponseDto(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }
}