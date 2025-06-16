package com.example.outsourcingproject.domain.user.exception;

import com.example.outsourcingproject.global.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.outsourcingproject.domain.user")
public class UserExceptionHandler {

    // User 도메인 부모 예외
    @ExceptionHandler(UserException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserException(UserException ex) {
        ApiResponse<Void> response = ApiResponse.failure(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
