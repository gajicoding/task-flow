package com.example.outsourcingproject.domain.task.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TaskErrorCode {

    //유저 관련
    TASK_CREATOR_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 생성자를 찾을 수 없습니다."),
    ASSIGNEE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 담당자를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),

    // 태스크 관련
    INVALID_TASK_TITLE(HttpStatus.BAD_REQUEST, "태스크 생성 시에는 제목을 필수로 입력해야 합니다"),
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 태스크를 찾을 수 없습니다."),

    // 작업 상태 관련
    INVALID_TASK_STATUS(HttpStatus.BAD_REQUEST, "잘못된 작업 상태 입니다."),
    TASK_STATUS_REQUIRED(HttpStatus.BAD_REQUEST, "변경할 작업 상태를 입력해주세요"),

    // 우선순위 관련
    INVALID_TASK_PRIORITY(HttpStatus.BAD_REQUEST, "잘못된 우선순위 입니다."),

    // 날짜 관련
    INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 날짜 형식입니다. 날짜는 yyyy-MM-dd HH:mm 형식으로 입력해야 합니다."),

    // JSON 문법 오류
    INVALID_JSON_SYNTAX(HttpStatus.BAD_REQUEST, "잘못된 요청 데이터의 형식입니다. 요청 데이터 마지막에 쉼표(,)가 있거나 구조가 올바르지 않습니다."),

    // 기타 오류
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청 데이터 형식입니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "요청 데이터가 올바르지 않습니다.");


    private final HttpStatus httpStatus;
    private final String message;
}
