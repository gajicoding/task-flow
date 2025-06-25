package com.example.outsourcingproject.domain.comment.exception;

import com.example.outsourcingproject.domain.comment.exception.error.CustomErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final CustomErrorCode errorCode;

    public CustomException(CustomErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
