package com.example.outsourcingproject.domain.auth.service;

import com.example.outsourcingproject.domain.auth.entity.RefreshToken;
import com.example.outsourcingproject.domain.auth.repository.RefreshTokenRepository;
import com.example.outsourcingproject.domain.auth.exception.InvalidCredentialsException;
import com.example.outsourcingproject.global.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Transactional
    public String createRefreshToken(Long userId) {

        refreshTokenRepository.deleteByUserId(userId);

        String token = UUID.randomUUID().toString();

        long refreshExpirationMillis = jwtProperties.getRefreshExpirationTime();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshExpirationMillis / 1000);

        RefreshToken refreshToken = new RefreshToken(token, userId, expiresAt);
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidCredentialsException("유효하지 않은 Refresh Token 입니다."));

        if (refreshToken.isExpired()) {
            throw new InvalidCredentialsException("만료된 Refresh Token 입니다.");
        }

        return refreshToken;
    }

    @Transactional
    public void deleteRefreshTokenByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteByExpiryTimeBefore(LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        deleteExpiredTokens();
    }
}