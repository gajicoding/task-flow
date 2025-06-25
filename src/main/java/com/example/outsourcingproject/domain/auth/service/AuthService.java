package com.example.outsourcingproject.domain.auth.service;

import com.example.outsourcingproject.domain.auth.dto.request.SigninRequestDto;
import com.example.outsourcingproject.domain.auth.dto.request.SignupRequestDto;
import com.example.outsourcingproject.domain.auth.dto.response.SigninResponseDto;
import com.example.outsourcingproject.domain.auth.dto.response.SignupResponseDto;
import com.example.outsourcingproject.domain.auth.entity.Auth;
import com.example.outsourcingproject.domain.auth.entity.RefreshToken;
import com.example.outsourcingproject.domain.auth.enums.AuthErrorMessage;
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
            throw new DuplicateEmailException(AuthErrorMessage.DUPLICATE_EMAIL.getMessage());
        }

        if (authRepository.existsByUsername(signupRequest.getUsername())) {
            throw new DuplicateEmailException(AuthErrorMessage.DUPLICATE_USERNAME.getMessage());
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
            throw new RateLimitException(AuthErrorMessage.ACCOUNT_BLOCKED.format(remainingMinutes),remainingMinutes);
        }

        Auth auth = authRepository.findActiveByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException(AuthErrorMessage.INVALID_CREDENTIALS.getMessage()));

        if (!passwordEncoder.matches(signinRequest.getPassword(), auth.getPassword())) {
            loginAttemptService.recordFailedAttempt(username);
            throw new InvalidCredentialsException(AuthErrorMessage.INVALID_CREDENTIALS.getMessage());
        }

        loginAttemptService.recordSuccessfulLogin(username);

        String accessToken = jwtTokenProvider.createToken(auth.getId(), auth.getEmail(), auth.getRole());
        String refreshToken = refreshTokenService.createRefreshToken(auth.getId());

        return new SigninResponseDto(auth.getId(), accessToken, refreshToken);
    }

    @Transactional
    public SigninResponseDto refreshToken(String refreshTokenValue) {

        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenValue);

        Auth auth = authRepository.findByIdAndIsDeletedFalse(refreshToken.getUserId())
                .orElseThrow(() -> new UserNotFoundException(AuthErrorMessage.USER_NOT_FOUND.getMessage()));

        String newAccessToken = jwtTokenProvider.createToken(
                auth.getId(),
                auth.getEmail(),
                auth.getRole()
        );

        String newRefreshToken = refreshTokenService.createRefreshToken(auth.getId());

        return new SigninResponseDto(auth.getId(), newAccessToken, newRefreshToken);
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
                .orElseThrow(() -> new InvalidCredentialsException(AuthErrorMessage.WITHDRAW_USER_NOT_FOUND.getMessage()));

        if (!passwordEncoder.matches(password, auth.getPassword())) {
            throw new InvalidCredentialsException(AuthErrorMessage.PASSWORD_MISMATCH.getMessage());
        }

        auth.delete();
        authRepository.save(auth);

        refreshTokenService.deleteRefreshTokenByUserId(userId);

        eventPublisher.publishEvent(new UserWithdrawnEvent(userId));
    }
}