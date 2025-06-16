package com.example.outsourcingproject.domain.auth.exception;

public class InvalidUserRoleException extends AuthException {
    public InvalidUserRoleException(String message) {
        super(message);
    }
}