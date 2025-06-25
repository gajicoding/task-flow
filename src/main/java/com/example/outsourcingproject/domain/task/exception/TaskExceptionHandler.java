package com.example.outsourcingproject.domain.task.exception;

import com.example.outsourcingproject.domain.task.enums.TaskErrorCode;
import com.example.outsourcingproject.global.dto.ApiResponse;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = "com.example.outsourcingproject.domain.task")
public class TaskExceptionHandler {

    // 커스텀 예외 처리
    @ExceptionHandler(TaskException.class)
    public ResponseEntity<ApiResponse<String>> handleTaskExceptions(TaskException ex) {
        TaskErrorCode error = ex.getErrorCode();
        return ResponseEntity.status(error.getHttpStatus()).body(ApiResponse.failure(error.getMessage(), error.name()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleJsonParseError(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException invalidEx) {
            Class<?> targetType = invalidEx.getTargetType();

            // InvalidFormatException 중에서 날짜 형식에 대해서만 예외 처리
            if (targetType == LocalDate.class || targetType == LocalDateTime.class) {
                return ResponseEntity.status(TaskErrorCode.INVALID_DATE_FORMAT.getHttpStatus()).body(ApiResponse.failure(TaskErrorCode.INVALID_DATE_FORMAT.getMessage(), TaskErrorCode.INVALID_DATE_FORMAT.name()));
            }
        }

        // JSON 문법 오류 시 예외 처리
        if (cause instanceof JsonParseException) {
            return ResponseEntity.status(TaskErrorCode.INVALID_JSON_SYNTAX.getHttpStatus()).body(ApiResponse.failure(TaskErrorCode.INVALID_JSON_SYNTAX.getMessage(), TaskErrorCode.INVALID_JSON_SYNTAX.name()));
        }

        // 기타 예외 처리
        return ResponseEntity.status(TaskErrorCode.INVALID_REQUEST.getHttpStatus()).body(ApiResponse.failure(TaskErrorCode.INVALID_REQUEST.getMessage(), TaskErrorCode.INVALID_REQUEST.name()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationError(MethodArgumentNotValidException ex) {

        FieldError fieldError = ex.getBindingResult().getFieldError();

        if (fieldError != null && "status".equals(fieldError.getField())) {
            TaskErrorCode error = TaskErrorCode.TASK_STATUS_REQUIRED;
            return ResponseEntity.status(TaskErrorCode.TASK_STATUS_REQUIRED.getHttpStatus()).body(ApiResponse.failure(TaskErrorCode.TASK_STATUS_REQUIRED.getMessage(), TaskErrorCode.TASK_STATUS_REQUIRED.name()));
        }

        return ResponseEntity.status(TaskErrorCode.VALIDATION_ERROR.getHttpStatus()).body(ApiResponse.failure(TaskErrorCode.VALIDATION_ERROR.getMessage(), TaskErrorCode.VALIDATION_ERROR.name()));

    }

}
