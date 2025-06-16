package com.example.outsourcingproject.domain.auth.controller;

import com.example.outsourcingproject.domain.auth.dto.request.RefreshTokenRequestDto;
import com.example.outsourcingproject.domain.auth.dto.request.SigninRequestDto;
import com.example.outsourcingproject.domain.auth.dto.request.SignupRequestDto;
import com.example.outsourcingproject.domain.auth.dto.request.WithdrawRequestDto;
import com.example.outsourcingproject.domain.auth.dto.response.SigninResponseDto;
import com.example.outsourcingproject.domain.auth.dto.response.SignupResponseDto;
import com.example.outsourcingproject.domain.auth.service.AuthService;
import com.example.outsourcingproject.global.dto.ApiResponse;
import com.example.outsourcingproject.global.security.JwtAuthenticationProvider;
import com.example.outsourcingproject.global.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    @PostMapping("/auth/signup")
    public ResponseEntity<ApiResponse<SignupResponseDto>> signup(@Valid @RequestBody SignupRequestDto signupRequest) {
        SignupResponseDto response = authService.signup(signupRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "회원가입이 완료되었습니다."));
    }

    @PostMapping("/auth/signin")
    public ResponseEntity<ApiResponse<SigninResponseDto>> signin(@Valid @RequestBody SigninRequestDto signinRequest) {
        SigninResponseDto response = authService.signin(signinRequest);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + response.getToken())
                .header("X-Refresh-Token", response.getRefreshToken())
                .body(ApiResponse.success("로그인이 완료되었습니다."));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<ApiResponse<SigninResponseDto>> refreshToken(@Valid @RequestBody RefreshTokenRequestDto refreshTokenRequest) {
        SigninResponseDto response = authService.refreshToken(refreshTokenRequest.getRefreshToken());

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + response.getToken())
                .header("X-Refresh-Token", response.getRefreshToken())
                .body(ApiResponse.success("토큰이 갱신되었습니다."));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                    HttpServletRequest request) {
        String accessToken = jwtAuthenticationProvider.extractTokenFromRequest(request);
        authService.logout(userPrincipal.getId(), accessToken);
        return ResponseEntity.ok(ApiResponse.success("로그아웃이 완료되었습니다."));
    }

    @PostMapping("/auth/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                      @Valid @RequestBody WithdrawRequestDto withdrawRequest) {
        authService.withdraw(userPrincipal.getId(), withdrawRequest.getPassword());
        return ResponseEntity.ok(ApiResponse.success("회원탈퇴가 완료되었습니다."));
    }
}