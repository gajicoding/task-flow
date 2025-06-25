package com.example.outsourcingproject.domain.auth.service;

import com.example.outsourcingproject.domain.auth.dto.request.SigninRequestDto;
import com.example.outsourcingproject.domain.auth.dto.request.SignupRequestDto;
import com.example.outsourcingproject.domain.auth.dto.response.SigninResponseDto;
import com.example.outsourcingproject.domain.auth.dto.response.SignupResponseDto;
import com.example.outsourcingproject.domain.auth.entity.Auth;
import com.example.outsourcingproject.domain.auth.entity.RefreshToken;
import com.example.outsourcingproject.domain.auth.enums.AuthErrorMessage;
import com.example.outsourcingproject.domain.auth.enums.UserRole;
import com.example.outsourcingproject.domain.auth.event.UserRegisteredEvent;
import com.example.outsourcingproject.domain.auth.event.UserWithdrawnEvent;
import com.example.outsourcingproject.domain.auth.exception.DuplicateEmailException;
import com.example.outsourcingproject.domain.auth.exception.InvalidCredentialsException;
import com.example.outsourcingproject.domain.auth.exception.RateLimitException;
import com.example.outsourcingproject.domain.auth.exception.UserNotFoundException;
import com.example.outsourcingproject.domain.auth.repository.AuthRepository;
import com.example.outsourcingproject.global.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthRepository authRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                authRepository,
                passwordEncoder,
                jwtTokenProvider,
                refreshTokenService,
                tokenBlacklistService,
                loginAttemptService,
                eventPublisher
        );
    }

    private SignupRequestDto createSignupRequest() {
        return new SignupRequestDto(
                "차준호",
                "juno",
                "ckwnsgh@example.com",
                "Password123!"
        );
    }

    private SigninRequestDto createSigninRequest() {
        return new SigninRequestDto("juno", "password123");
    }

    private Auth createAuth() {
        Auth auth = new Auth("차준호", "juno", "ckwnsgh@example.com", "encodedPassword", UserRole.USER);
        setId(auth);
        return auth;
    }

    private void setId(Auth auth) {
        try {
            java.lang.reflect.Field idField = Auth.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(auth, 1L);
        } catch (Exception e) {
            throw new RuntimeException("ID 설정 실패", e);
        }
    }

    @DisplayName("회원가입 성공 테스트")
    @Test
    void signup_Success() {
        // given
        SignupRequestDto signupRequest = createSignupRequest();

        given(authRepository.existsByEmail(signupRequest.getEmail())).willReturn(false);
        given(authRepository.existsByUsername(signupRequest.getUsername())).willReturn(false);
        given(passwordEncoder.encode(signupRequest.getPassword())).willReturn("encodedPassword");

        Auth savedAuth = createAuth();
        given(authRepository.save(any(Auth.class))).willReturn(savedAuth);

        // when
        SignupResponseDto result = authService.signup(signupRequest);

        // then
        assertThat(result.getUsername()).isEqualTo(signupRequest.getUsername());
        assertThat(result.getEmail()).isEqualTo(signupRequest.getEmail());

        // 이벤트 발행 검증
        ArgumentCaptor<UserRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        UserRegisteredEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getUserId()).isEqualTo(1L);
        assertThat(publishedEvent.getName()).isEqualTo(signupRequest.getName());
        assertThat(publishedEvent.getEmail()).isEqualTo(signupRequest.getEmail());
    }

    @DisplayName("회원가입 실패 : 이메일 중복")
    @Test
    void signup_DuplicateEmail_ThrowsException() {
        // given
        SignupRequestDto signupRequest = new SignupRequestDto(
                "차준호", "junojuno", "ckwnsgh@example.com", "Password123!");

        given(authRepository.existsByEmail(signupRequest.getEmail())).willReturn(true);

        // when and then
        assertThatThrownBy(() -> authService.signup(signupRequest))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage(AuthErrorMessage.DUPLICATE_EMAIL.getMessage());

        verify(authRepository, never()).save(any(Auth.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @DisplayName("회원가입 실패 : 사용자명 중복")
    @Test
    void signup_DuplicateUsername_ThrowsException() {
        // given
        SignupRequestDto signupRequest = new SignupRequestDto(
                "차준호차", "juno", "test@example.com", "Password123!");

        given(authRepository.existsByEmail(signupRequest.getEmail())).willReturn(false);
        given(authRepository.existsByUsername(signupRequest.getUsername())).willReturn(true);

        // when and then
        assertThatThrownBy(() -> authService.signup(signupRequest))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage(AuthErrorMessage.DUPLICATE_USERNAME.getMessage());

        verify(authRepository, never()).save(any(Auth.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @DisplayName("로그인 성공 테스트")
    @Test
    void signin_Success() {
        // given
        SigninRequestDto signinRequest = createSigninRequest();
        Auth auth = createAuth();

        given(loginAttemptService.isBlocked(signinRequest.getUsername())).willReturn(false);
        given(authRepository.findActiveByUsername(signinRequest.getUsername())).willReturn(Optional.of(auth));
        given(passwordEncoder.matches(signinRequest.getPassword(), auth.getPassword())).willReturn(true);
        given(jwtTokenProvider.createToken(auth.getId(), auth.getEmail(), auth.getRole())).willReturn("accessToken");
        given(refreshTokenService.createRefreshToken(auth.getId())).willReturn("refreshToken");

        // when
        SigninResponseDto result = authService.signin(signinRequest);

        // then
        assertThat(result.getToken()).isEqualTo("accessToken");
        assertThat(result.getRefreshToken()).isEqualTo("refreshToken");

        verify(loginAttemptService).recordSuccessfulLogin(signinRequest.getUsername());
    }

    @DisplayName("로그인 실패 : 사용자 없음")
    @Test
    void signin_UserNotFound_ThrowsException() {
        // given
        SigninRequestDto signinRequest = new SigninRequestDto("notExit", "password123");

        given(loginAttemptService.isBlocked(signinRequest.getUsername())).willReturn(false);
        given(authRepository.findActiveByUsername(signinRequest.getUsername())).willReturn(Optional.empty());

        // when and then
        assertThatThrownBy(() -> authService.signin(signinRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage(AuthErrorMessage.INVALID_CREDENTIALS.getMessage());


        verify(loginAttemptService, never()).recordSuccessfulLogin(any());
    }

    @DisplayName("로그인 실패 : 비밀번호 불일치")
    @Test
    void signin_WrongPassword_ThrowsException() {
        // given
        SigninRequestDto signinRequest = new SigninRequestDto("juno", "wrongpassword");
        Auth auth = createAuth();

        given(loginAttemptService.isBlocked(signinRequest.getUsername())).willReturn(false);
        given(authRepository.findActiveByUsername(signinRequest.getUsername())).willReturn(Optional.of(auth));
        given(passwordEncoder.matches(signinRequest.getPassword(), auth.getPassword())).willReturn(false);

        // when and then
        assertThatThrownBy(() -> authService.signin(signinRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage(AuthErrorMessage.INVALID_CREDENTIALS.getMessage());

        verify(loginAttemptService).recordFailedAttempt(signinRequest.getUsername());
        verify(loginAttemptService, never()).recordSuccessfulLogin(any());
    }

    @DisplayName("로그인 실패 : 계정 차단")
    @Test
    void signin_AccountBlocked_ThrowsException() {
        // given
        SigninRequestDto signinRequest = new SigninRequestDto("blockeduser", "password123");

        given(loginAttemptService.isBlocked(signinRequest.getUsername())).willReturn(true);
        given(loginAttemptService.getRemainingBlockTimeMinutes(signinRequest.getUsername())).willReturn(10L);

        // when and then
        assertThatThrownBy(() -> authService.signin(signinRequest))
                .isInstanceOf(RateLimitException.class)
                .hasMessage(AuthErrorMessage.ACCOUNT_BLOCKED.format(10L));

        verify(authRepository, never()).findActiveByUsername(any());
    }

    @DisplayName("토큰 갱신 성공 테스트")
    @Test
    void refreshToken_Success() {
        // given
        String refreshTokenValue = "validRefreshToken";

        RefreshToken refreshToken = mock(RefreshToken.class);
        given(refreshToken.getUserId()).willReturn(1L);

        Auth auth = createAuth();

        given(refreshTokenService.validateRefreshToken(refreshTokenValue)).willReturn(refreshToken);
        given(authRepository.findByIdAndIsDeletedFalse(1L)).willReturn(Optional.of(auth));
        given(jwtTokenProvider.createToken(auth.getId(), auth.getEmail(), auth.getRole())).willReturn("newAccessToken");
        given(refreshTokenService.createRefreshToken(auth.getId())).willReturn("newRefreshToken");

        // when
        SigninResponseDto result = authService.refreshToken(refreshTokenValue);

        // then
        assertThat(result.getToken()).isEqualTo("newAccessToken");
        assertThat(result.getRefreshToken()).isEqualTo("newRefreshToken");
    }

    @DisplayName("토큰 갱신 실패 - 사용자 없음")
    @Test
    void refreshToken_UserNotFound_ThrowsException() {
        // given
        String refreshTokenValue = "validRefreshToken";

        RefreshToken refreshToken = mock(RefreshToken.class);
        given(refreshToken.getUserId()).willReturn(999L);

        given(refreshTokenService.validateRefreshToken(refreshTokenValue)).willReturn(refreshToken);
        given(authRepository.findByIdAndIsDeletedFalse(999L)).willReturn(Optional.empty());

        // when and then
        assertThatThrownBy(() -> authService.refreshToken(refreshTokenValue))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(AuthErrorMessage.USER_NOT_FOUND.getMessage());
    }

    @DisplayName("로그아웃 성공 테스트")
    @Test
    void logout_Success() {
        // given
        Long userId = 1L;
        String accessToken = "validAccessToken";

        // when
        authService.logout(userId, accessToken);

        // then
        verify(tokenBlacklistService).addTokenToBlacklist(accessToken, userId);
        verify(refreshTokenService).deleteRefreshTokenByUserId(userId);
    }

    @DisplayName("로그아웃 : 토큰이 null인 경우")
    @Test
    void logout_NullToken() {
        // given
        Long userId = 1L;

        // when
        authService.logout(userId, null);

        // then
        verify(tokenBlacklistService, never()).addTokenToBlacklist(any(), any());
        verify(refreshTokenService).deleteRefreshTokenByUserId(userId);
    }

    @DisplayName("회원탈퇴 성공 테스트")
    @Test
    void withdraw_Success() {
        // given
        Long userId = 1L;
        String password = "password123";

        Auth auth = spy(createAuth());

        given(authRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(auth));
        given(passwordEncoder.matches(password, auth.getPassword())).willReturn(true);

        // when
        authService.withdraw(userId, password);

        // then
        verify(auth).delete();
        verify(authRepository).save(auth);
        verify(refreshTokenService).deleteRefreshTokenByUserId(userId);

        ArgumentCaptor<UserWithdrawnEvent> eventCaptor = ArgumentCaptor.forClass(UserWithdrawnEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getUserId()).isEqualTo(userId);
    }

    @DisplayName("회원탈퇴 실패 : 사용자 없음")
    @Test
    void withdraw_UserNotFound_ThrowsException() {
        // given
        Long userId = 999L;
        String password = "password123";

        given(authRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.empty());

        // when and then
        assertThatThrownBy(() -> authService.withdraw(userId, password))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage(AuthErrorMessage.WITHDRAW_USER_NOT_FOUND.getMessage());

        verify(refreshTokenService, never()).deleteRefreshTokenByUserId(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @DisplayName("회원탈퇴 실패 : 비밀번호 불일치")
    @Test
    void withdraw_WrongPassword_ThrowsException() {
        // given
        Long userId = 1L;
        String wrongPassword = "wrongpassword";

        Auth auth = createAuth();

        given(authRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(auth));
        given(passwordEncoder.matches(wrongPassword, auth.getPassword())).willReturn(false);

        // when and then
        assertThatThrownBy(() -> authService.withdraw(userId, wrongPassword))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage(AuthErrorMessage.PASSWORD_MISMATCH.getMessage());

        verify(authRepository, never()).save(any());
        verify(refreshTokenService, never()).deleteRefreshTokenByUserId(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
