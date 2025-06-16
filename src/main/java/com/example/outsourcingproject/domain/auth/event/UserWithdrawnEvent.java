package com.example.outsourcingproject.domain.auth.event;

import lombok.Getter;

@Getter
public class UserWithdrawnEvent {

    private final Long userId;

    public UserWithdrawnEvent(Long userId) {
        this.userId = userId;
    }
}
