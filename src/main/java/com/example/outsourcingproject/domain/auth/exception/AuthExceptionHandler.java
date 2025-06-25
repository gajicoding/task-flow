package com.example.outsourcingproject.domain.auth.exception;

import com.example.outsourcingproject.domain.auth.enums.AuthErrorMessage;
import com.example.outsourcingproject.global.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.example.outsourcingproject.domain.auth")
public class AuthExceptionHandler {

  // Bean Validation 예외 처리 (Auth 도메인 전용)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {

    // 첫 번째 에러만 가져와서 간단한 메시지로 처리
    String errorMessage = AuthErrorMessage.VALIDATION_FAILED.getMessage();

    if (ex.getBindingResult().hasFieldErrors()) {
      FieldError fieldError = ex.getBindingResult().getFieldErrors().get(0);
      errorMessage = fieldError.getDefaultMessage();
    }

    ApiResponse<Void> response = ApiResponse.failure(errorMessage);
    return ResponseEntity.status(AuthErrorMessage.VALIDATION_FAILED.getStatus()).body(response);
  }

  // 409 - 이메일 중복
  @ExceptionHandler(DuplicateEmailException.class)
  public ResponseEntity<ApiResponse<Void>> handleDuplicateEmail(DuplicateEmailException ex) {
    ApiResponse<Void> response = ApiResponse.failure(ex.getMessage());
    return ResponseEntity.status(AuthErrorMessage.DUPLICATE_EMAIL.getStatus()).body(response);
  }

  // 401 - 로그인 실패, 토큰 오류
  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
    ApiResponse<Void> response = ApiResponse.failure(ex.getMessage());
    return ResponseEntity.status(AuthErrorMessage.INVALID_CREDENTIALS.getStatus()).body(response);
  }

  @ExceptionHandler(InvalidUserRoleException.class)
  public ResponseEntity<ApiResponse<Void>> handleInvalidUserRole(InvalidUserRoleException ex) {
    ApiResponse<Void> response = ApiResponse.failure(ex.getMessage());
    return ResponseEntity.status(AuthErrorMessage.INVALID_USER_ROLE.getStatus()).body(response);
  }

  // 404 - 사용자 없음
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException ex) {
    ApiResponse<Void> response = ApiResponse.failure(ex.getMessage());
    return ResponseEntity.status(AuthErrorMessage.USER_NOT_FOUND.getStatus()).body(response);
  }

  // 429 - 로그인 시도 제한
  @ExceptionHandler(RateLimitException.class)
  public ResponseEntity<ApiResponse<Map<String, Object>>> handleRateLimitException(RateLimitException ex) {
    Map<String, Object> data = new HashMap<>();
    data.put("remainingTimeMinutes", ex.getRemainingTimeMinutes());
    data.put("retryAfter", ex.getRemainingTimeMinutes() * 60);

    ApiResponse<Map<String, Object>> response = ApiResponse.failure(ex.getMessage(), data);
    return ResponseEntity.status(AuthErrorMessage.ACCOUNT_BLOCKED.getStatus()).body(response);
  }

  // 500 - 서버 오류
  @ExceptionHandler(ServerException.class)
  public ResponseEntity<ApiResponse<Void>> handleServerException(ServerException ex) {
    ApiResponse<Void> response = ApiResponse.failure(ex.getMessage());
    return ResponseEntity.status(AuthErrorMessage.INTERNAL_SERVER_ERROR.getStatus()).body(response);
  }

  // AuthException 부모 예외 (위에서 처리되지 않은 경우)
  @ExceptionHandler(AuthException.class)
  public ResponseEntity<ApiResponse<Void>> handleAuthException(AuthException ex) {
    ApiResponse<Void> response = ApiResponse.failure(ex.getMessage());
    return ResponseEntity.status(AuthErrorMessage.INTERNAL_SERVER_ERROR.getStatus()).body(response);
  }
}
