package com.example.outsourcingproject.global.exception;

import com.example.outsourcingproject.domain.auth.exception.*;
import com.example.outsourcingproject.domain.user.exception.InvalidUserRoleException;
import com.example.outsourcingproject.domain.user.exception.UserException;
import com.example.outsourcingproject.global.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ApiResponse<Map<String, String>> response = ApiResponse.failure("입력값이 올바르지 않습니다.", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 409 - 이메일 중복
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmail(DuplicateEmailException ex) {
        ApiResponse<Void> response = ApiResponse.failure(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // 401 - 로그인 실패, 토큰 오류
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        ApiResponse<Void> response = ApiResponse.failure(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // 404 - 사용자 없음
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException ex) {
        ApiResponse<Void> response = ApiResponse.failure(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // 429 - 로그인 시도 제한
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleRateLimitException(RateLimitException ex) {
        Map<String, Object> data = new HashMap<>();
        data.put("remainingTimeMinutes", ex.getRemainingTimeMinutes());
        data.put("retryAfter", ex.getRemainingTimeMinutes() * 60);

        ApiResponse<Map<String, Object>> response = ApiResponse.failure(ex.getMessage(), data);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    // AuthException 부모 예외 (위에서 처리되지 않은 경우)
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthException(AuthException ex) {
        ApiResponse<Void> response = ApiResponse.failure(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // 서버 오류
    @ExceptionHandler(ServerException.class)
    public ResponseEntity<ApiResponse<Void>> handleServerException(ServerException ex) {
        ApiResponse<Void> response = ApiResponse.failure(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(InvalidUserRoleException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidUserRole(InvalidUserRoleException ex) {
        ApiResponse<Void> response = ApiResponse.failure(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);  // 400
    }

    // User 도메인 부모 예외
    @ExceptionHandler(UserException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserException(UserException ex) {
        ApiResponse<Void> response = ApiResponse.failure(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);  // 400
    }

    // 전체 예외 (최후의 보루)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException() {
        ApiResponse<Void> response = ApiResponse.failure("서버 내부 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}