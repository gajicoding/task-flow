package com.example.outsourcingproject.domain.auth.exception;

import lombok.Getter;

@Getter
public class RateLimitException extends AuthException {
    private final long remainingTimeMinutes;

    public RateLimitException(String message, long remainingTimeMinutes) {
        super(message);
        this.remainingTimeMinutes = remainingTimeMinutes;
    }
}