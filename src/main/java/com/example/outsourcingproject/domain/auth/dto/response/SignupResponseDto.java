package com.example.outsourcingproject.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class SignupResponseDto {

    private final String username;
    private final String email;

    public SignupResponseDto(String username, String email) {
        this.username = username;
        this.email = email;
    }
}