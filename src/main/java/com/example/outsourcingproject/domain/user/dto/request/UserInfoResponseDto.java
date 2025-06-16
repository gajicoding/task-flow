package com.example.outsourcingproject.domain.user.dto.request;

import com.example.outsourcingproject.domain.user.entity.User;
import lombok.Getter;

@Getter
public class UserInfoResponseDto {

    private final Long id;
    private final String name;
    private final String email;

    public UserInfoResponseDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
    }
}
