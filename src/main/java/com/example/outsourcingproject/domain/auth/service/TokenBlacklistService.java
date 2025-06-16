package com.example.outsourcingproject.domain.auth.service;

import com.example.outsourcingproject.domain.auth.entity.TokenBlacklist;
import com.example.outsourcingproject.domain.auth.repository.TokenBlacklistRepository;
import com.example.outsourcingproject.global.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void addTokenToBlacklist(String accessToken, Long userId) {
        Claims claims = jwtTokenProvider.parseToken(accessToken);
        String jti = claims.getId();

        if (StringUtils.hasText(jti)) {
            LocalDateTime expiryTime = jwtTokenProvider.getExpirationTime(claims);

            if (expiryTime.isAfter(LocalDateTime.now())) {
                if (!tokenBlacklistRepository.existsByJti(jti)) {
                    TokenBlacklist blacklistToken = new TokenBlacklist(jti, userId, expiryTime);
                    tokenBlacklistRepository.save(blacklistToken);
                }
            }
        }
    }

    public boolean isBlacklisted(String jti) {
        if (!StringUtils.hasText(jti)) {
            return false;
        }

        return tokenBlacklistRepository.existsByJtiAndExpiryTimeAfter(jti, LocalDateTime.now());
    }

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenBlacklistRepository.deleteExpiredTokens(now);
    }
}