package com.example.outsourcingproject.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequestDto {

    @NotBlank(message = "Refresh Token은 필수입니다")
    @Size(min = 10, message = "올바른 Refresh Token 형식이 아닙니다")
    private String refreshToken;
}