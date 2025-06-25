package com.example.outsourcingproject.global.exception;

import com.example.outsourcingproject.global.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {

        String message = String.format("%s 메서드는 지원되지 않습니다. 지원되는 메서드: %s",
                ex.getMethod(), ex.getSupportedHttpMethods());

        ApiResponse<Void> response = ApiResponse.failure(message);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    // 예상치 못한 전체 예외 (최후의 보루)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException() {
        String message = "서버 내부 오류가 발생했습니다.";
        ApiResponse<Void> response = ApiResponse.failure(message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}