package com.example.outsourcingproject.domain.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService();
    }

    @DisplayName("로그인 실패 횟수 기록 테스트")
    @Test
    void recordFailedAttempt_ShouldIncrementAttemptCount() {
        // given
        String username = "testuser";

        // when
        loginAttemptService.recordFailedAttempt(username);

        // then
        assertThat(loginAttemptService.isBlocked(username)).isFalse(); // 1회 실패로는 차단되지 않음
    }

    @DisplayName("5회 실패 후 계정 차단 테스트")
    @Test
    void recordFailedAttempt_FiveTimes_ShouldBlockAccount() {
        // given
        String username = "testuser";

        // when
        for (int i = 0; i < 5; i++) {
            loginAttemptService.recordFailedAttempt(username);
        }

        // then
        assertThat(loginAttemptService.isBlocked(username)).isTrue();
        assertThat(loginAttemptService.getRemainingBlockTimeMinutes(username)).isGreaterThan(0);
    }

    @DisplayName("성공적인 로그인 후 실패 횟수 초기화 테스트")
    @Test
    void recordSuccessfulLogin_ShouldResetAttemptCount() {
        // given
        String username = "testuser";

        // 3회 실패 기록
        for (int i = 0; i < 3; i++) {
            loginAttemptService.recordFailedAttempt(username);
        }

        // when
        loginAttemptService.recordSuccessfulLogin(username);

        // then
        assertThat(loginAttemptService.isBlocked(username)).isFalse();
    }
}
