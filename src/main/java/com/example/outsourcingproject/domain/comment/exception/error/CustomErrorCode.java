package com.example.outsourcingproject.domain.comment.exception.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CustomErrorCode {

    // 예시 에러 코드들
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "태스크를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "접근 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    CustomErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
