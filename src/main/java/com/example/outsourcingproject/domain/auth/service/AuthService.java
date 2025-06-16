package com.example.outsourcingproject.domain.auth.service;

import com.example.outsourcingproject.domain.auth.dto.request.SigninRequestDto;
import com.example.outsourcingproject.domain.auth.dto.request.SignupRequestDto;
import com.example.outsourcingproject.domain.auth.dto.response.SigninResponseDto;
import com.example.outsourcingproject.domain.auth.dto.response.SignupResponseDto;
import com.example.outsourcingproject.domain.auth.entity.Auth;
import com.example.outsourcingproject.domain.auth.entity.RefreshToken;
import com.example.outsourcingproject.domain.auth.event.UserRegisteredEvent;
import com.example.outsourcingproject.domain.auth.event.UserWithdrawnEvent;
import com.example.outsourcingproject.domain.auth.exception.DuplicateEmailException;
import com.example.outsourcingproject.domain.auth.exception.InvalidCredentialsException;
import com.example.outsourcingproject.domain.auth.exception.RateLimitException;
import com.example.outsourcingproject.domain.auth.exception.UserNotFoundException;
import com.example.outsourcingproject.domain.auth.enums.UserRole;
import com.example.outsourcingproject.domain.auth.repository.AuthRepository;
import com.example.outsourcingproject.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final LoginAttemptService loginAttemptService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public SignupResponseDto signup(SignupRequestDto signupRequest) {

        if (authRepository.existsByEmail(signupRequest.getEmail())) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다.");
        }

        if (authRepository.existsByUsername(signupRequest.getUsername())) {
            throw new DuplicateEmailException("이미 존재하는 사용자명입니다.");
        }

        UserRole userRole = UserRole.USER;

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        Auth auth = new Auth(
                signupRequest.getName(),
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                encodedPassword,
                userRole
        );

        Auth savedAuth = authRepository.save(auth);

        eventPublisher.publishEvent(new UserRegisteredEvent(
                savedAuth.getId(),
                savedAuth.getName(),
                savedAuth.getEmail()
        ));

        return new SignupResponseDto(savedAuth.getUsername(), savedAuth.getEmail());
    }

    @Transactional
    public SigninResponseDto signin(SigninRequestDto signinRequest) {
        String username = signinRequest.getUsername();

        if (loginAttemptService.isBlocked(username)) {
            long remainingMinutes = loginAttemptService.getRemainingBlockTimeMinutes(username);
            throw new RateLimitException("로그인이 일시적으로 차단되었습니다.", remainingMinutes);
        }

        Auth auth = authRepository.findActiveByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("잘못된 사용자명 또는 비밀번호입니다"));

        if (!passwordEncoder.matches(signinRequest.getPassword(), auth.getPassword())) {
            loginAttemptService.recordFailedAttempt(username);
            throw new InvalidCredentialsException("잘못된 사용자명 또는 비밀번호입니다");
        }

        loginAttemptService.recordSuccessfulLogin(username);

        String accessToken = jwtTokenProvider.createToken(auth.getId(), auth.getEmail(), auth.getRole());
        String refreshToken = refreshTokenService.createRefreshToken(auth.getId());

        return new SigninResponseDto(accessToken, refreshToken);
    }

    @Transactional
    public SigninResponseDto refreshToken(String refreshTokenValue) {

        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenValue);

        Auth auth = authRepository.findByIdAndIsDeletedFalse(refreshToken.getUserId())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        String newAccessToken = jwtTokenProvider.createToken(
                auth.getId(),
                auth.getEmail(),
                auth.getRole()
        );

        String newRefreshToken = refreshTokenService.createRefreshToken(auth.getId());

        return new SigninResponseDto(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(Long userId, String accessToken) {
        if (StringUtils.hasText(accessToken)) {
            tokenBlacklistService.addTokenToBlacklist(accessToken, userId);
        }

        refreshTokenService.deleteRefreshTokenByUserId(userId);
    }

    @Transactional
    public void withdraw(Long userId, String password) {
        Auth auth = authRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new InvalidCredentialsException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, auth.getPassword())) {
            throw new InvalidCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        auth.delete();
        authRepository.save(auth);

        refreshTokenService.deleteRefreshTokenByUserId(userId);

        eventPublisher.publishEvent(new UserWithdrawnEvent(userId));
    }
}