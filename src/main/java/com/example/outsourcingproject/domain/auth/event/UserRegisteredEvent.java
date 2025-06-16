package com.example.outsourcingproject.domain.auth.event;

import lombok.Getter;

@Getter
public class UserRegisteredEvent {

    private final Long userId;
    private final String name;
    private final String email;

    public UserRegisteredEvent(Long userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }
}
