package com.example.outsourcingproject.domain.auth.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorMessage {

    // 회원가입 관련
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH001", "이미 사용 중인 이메일입니다. 다른 이메일을 사용해주세요."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "AUTH002", "이미 사용 중인 사용자명입니다. 다른 사용자명을 사용해주세요."),

    // 로그인 관련
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH003", "사용자명 또는 비밀번호가 올바르지 않습니다."),
    ACCOUNT_BLOCKED(HttpStatus.TOO_MANY_REQUESTS, "AUTH004", "로그인 시도가 많아 계정이 일시적으로 차단되었습니다. %d분 후에 다시 시도해주세요."),

    // 토큰 관련
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH005", "유효하지 않은 Refresh Token 입니다. 다시 로그인해주세요."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH006", "Refresh Token이 만료되었습니다. 다시 로그인해주세요."),

    // 사용자 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH007", "사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요."),
    USER_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH008", "사용자 프로필을 찾을 수 없습니다."),

    // 회원탈퇴 관련
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH009", "현재 비밀번호가 일치하지 않습니다. 다시 확인해주세요."),
    WITHDRAW_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH010", "사용자 정보를 찾을 수 없습니다."),

    // 검증 관련
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "AUTH011", "입력값이 올바르지 않습니다."),

    // 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH012", "처리 중 오류가 발생했습니다."),

    // 권한 관련
    INVALID_USER_ROLE(HttpStatus.BAD_REQUEST, "AUTH013", "유효하지 않은 사용자 권한입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    AuthErrorMessage(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public String format(Object... args) {
        return String.format(this.message, args);
    }
}
