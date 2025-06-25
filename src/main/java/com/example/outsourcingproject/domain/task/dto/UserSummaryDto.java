package com.example.outsourcingproject.domain.task.dto;

import com.example.outsourcingproject.domain.user.entity.User;
import lombok.Getter;

@Getter
public class UserSummaryDto {

    private Long id;
    private String name;

    public UserSummaryDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
    }
}
