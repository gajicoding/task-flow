package com.example.outsourcingproject.domain.auth.exception;

public abstract class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}