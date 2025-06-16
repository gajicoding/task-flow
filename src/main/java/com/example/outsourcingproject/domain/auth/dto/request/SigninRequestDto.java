package com.example.outsourcingproject.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SigninRequestDto {

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(max = 100, message = "아이디는 100자를 초과할 수 없습니다")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 1, max = 255, message = "비밀번호를 입력해주세요")
    private String password;
}