package com.example.outsourcingproject.domain.auth.service;

import com.example.outsourcingproject.domain.auth.dto.request.SigninRequestDto;
import com.example.outsourcingproject.domain.auth.dto.request.SignupRequestDto;
import com.example.outsourcingproject.domain.auth.dto.response.SigninResponseDto;
import com.example.outsourcingproject.domain.auth.dto.response.SignupResponseDto;
import com.example.outsourcingproject.domain.auth.entity.RefreshToken;
import com.example.outsourcingproject.domain.auth.exception.DuplicateEmailException;
import com.example.outsourcingproject.domain.auth.exception.InvalidCredentialsException;
import com.example.outsourcingproject.domain.auth.exception.RateLimitException;
import com.example.outsourcingproject.domain.auth.exception.UserNotFoundException;
import com.example.outsourcingproject.domain.user.entity.User;
import com.example.outsourcingproject.domain.user.enums.UserRole;
import com.example.outsourcingproject.domain.user.repository.UserRepository;
import com.example.outsourcingproject.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final LoginAttemptService loginAttemptService;

    @Transactional
    public SignupResponseDto signup(SignupRequestDto signupRequest) {

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다.");
        }

        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new DuplicateEmailException("이미 존재하는 사용자명입니다.");
        }

        UserRole userRole = UserRole.USER;

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        User newUser = new User(
                signupRequest.getName(),
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                encodedPassword,
                userRole
        );
        User savedUser = userRepository.save(newUser);

        String accessToken = jwtTokenProvider.createToken(savedUser.getId(), savedUser.getEmail(), userRole);
        String refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());

        return new SignupResponseDto(accessToken, refreshToken);
    }

    @Transactional
    public SigninResponseDto signin(SigninRequestDto signinRequest) {
        String email = signinRequest.getEmail();

        if (loginAttemptService.isBlocked(email)) {
            long remainingMinutes = loginAttemptService.getRemainingBlockTimeMinutes(email);
            throw new RateLimitException("로그인이 일시적으로 차단되었습니다.", remainingMinutes);
        }

        User user = userRepository.findActiveByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않다."));

        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            loginAttemptService.recordFailedAttempt(email);
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = refreshTokenService.createRefreshToken(user.getId());

        loginAttemptService.recordSuccessfulLogin(email);

        return new SigninResponseDto(accessToken, refreshToken);
    }

    @Transactional
    public SigninResponseDto refreshToken(String refreshTokenValue) {

        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenValue);

        User user = userRepository.findByIdAndIsDeletedFalse(refreshToken.getUserId())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        String newAccessToken = jwtTokenProvider.createToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        String newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

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
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new InvalidCredentialsException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        user.delete();
        userRepository.save(user);

        refreshTokenService.deleteRefreshTokenByUserId(userId);
    }
}