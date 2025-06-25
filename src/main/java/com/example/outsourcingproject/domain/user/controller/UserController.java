package com.example.outsourcingproject.domain.user.controller;

import com.example.outsourcingproject.domain.user.dto.UserResponseDto;
import com.example.outsourcingproject.domain.user.dto.request.UserInfoResponseDto;
import com.example.outsourcingproject.domain.user.service.UserService;
import com.example.outsourcingproject.global.dto.ApiResponse;
import com.example.outsourcingproject.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/me")
    public ResponseEntity<ApiResponse<UserInfoResponseDto>> getUserInfo(
            @AuthenticationPrincipal UserPrincipal userPrincipal)
    {
        UserInfoResponseDto response = userService.getUserInfo(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "사용자 정보를 조회했습니다."));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getUserALl()
    {
        List<UserResponseDto> response = userService.getUserAll();
        return ResponseEntity.ok(ApiResponse.success(response, "사용자 목록을 조회했습니다."));
    }
}
