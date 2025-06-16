package com.example.outsourcingproject.domain.auth.exception;

import com.example.outsourcingproject.global.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.example.outsourcingproject.domain.auth")
public class AuthExceptionHandler{
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

  @ExceptionHandler(InvalidUserRoleException.class)
  public ResponseEntity<ApiResponse<Void>> handleInvalidUserRole(InvalidUserRoleException ex) {
    ApiResponse<Void> response = ApiResponse.failure(ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
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

  // 500 - 서버 오류
  @ExceptionHandler(ServerException.class)
  public ResponseEntity<ApiResponse<Void>> handleServerException(ServerException ex) {
    ApiResponse<Void> response = ApiResponse.failure(ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  // AuthException 부모 예외 (위에서 처리되지 않은 경우)
  @ExceptionHandler(AuthException.class)
  public ResponseEntity<ApiResponse<Void>> handleAuthException(AuthException ex) {
    ApiResponse<Void> response = ApiResponse.failure(ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }
}
