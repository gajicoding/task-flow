package com.example.outsourcingproject.domain.auth.exception;

public class DuplicateEmailException extends AuthException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}