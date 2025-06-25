package com.example.outsourcingproject.domain.auth.service;

import com.example.outsourcingproject.domain.auth.entity.RefreshToken;
import com.example.outsourcingproject.domain.auth.enums.AuthErrorMessage;
import com.example.outsourcingproject.domain.auth.exception.InvalidCredentialsException;
import com.example.outsourcingproject.domain.auth.repository.RefreshTokenRepository;
import com.example.outsourcingproject.global.security.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProperties jwtProperties;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, jwtProperties);
    }

    @DisplayName("리프레시 토큰 생성 테스트")
    @Test
    void createRefreshToken_ShouldReturnTokenString() {
        // given
        Long userId = 1L;
        given(jwtProperties.getRefreshExpirationTime()).willReturn(1_209_600_000L); // 14일

        // when
        String token = refreshTokenService.createRefreshToken(userId);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        verify(refreshTokenRepository).deleteByUserId(userId); // 기존 토큰 삭제 확인
        verify(refreshTokenRepository).save(any(RefreshToken.class)); // 새 토큰 저장 확인
    }

    @DisplayName("유효한 리프레시 토큰 검증 테스트")
    @Test
    void validateRefreshToken_ValidToken_ShouldReturnRefreshToken() {
        // given
        String tokenValue = "valid-refresh-token";
        RefreshToken refreshToken = mock(RefreshToken.class);

        given(refreshTokenRepository.findByToken(tokenValue)).willReturn(Optional.of(refreshToken));
        given(refreshToken.isExpired()).willReturn(false);

        // when
        RefreshToken result = refreshTokenService.validateRefreshToken(tokenValue);

        // then
        assertThat(result).isEqualTo(refreshToken);
    }

    @DisplayName("존재하지 않는 리프레시 토큰 검증 실패 테스트")
    @Test
    void validateRefreshToken_NonExistentToken_ShouldThrowException() {
        // given
        String tokenValue = "non-existent-token";
        given(refreshTokenRepository.findByToken(tokenValue)).willReturn(Optional.empty());

        // when and then
        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken(tokenValue))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage(AuthErrorMessage.INVALID_REFRESH_TOKEN.getMessage());
    }

    @DisplayName("만료된 리프레시 토큰 검증 실패 테스트")
    @Test
    void validateRefreshToken_ExpiredToken_ShouldThrowException() {
        // given
        String tokenValue = "expired-refresh-token";
        RefreshToken refreshToken = mock(RefreshToken.class);

        given(refreshTokenRepository.findByToken(tokenValue)).willReturn(Optional.of(refreshToken));
        given(refreshToken.isExpired()).willReturn(true);

        // when and then
        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken(tokenValue))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage(AuthErrorMessage.EXPIRED_REFRESH_TOKEN.getMessage());
    }

    @DisplayName("사용자별 리프레시 토큰 삭제 테스트")
    @Test
    void deleteRefreshTokenByUserId_ShouldDeleteToken() {
        // given
        Long userId = 1L;

        // when
        refreshTokenService.deleteRefreshTokenByUserId(userId);

        // then
        verify(refreshTokenRepository).deleteByUserId(userId);
    }

    @DisplayName("만료된 토큰들 정리 테스트")
    @Test
    void deleteExpiredTokens_ShouldDeleteExpiredTokens() {
        // when
        refreshTokenService.deleteExpiredTokens();

        // then
        verify(refreshTokenRepository).deleteByExpiryTimeBefore(any(LocalDateTime.class));
    }
}
