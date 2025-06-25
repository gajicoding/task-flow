package com.example.outsourcingproject.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(staticName = "of")
public class ActivityFeedDto {

    private String userName;
    private String message;
    private LocalDateTime activityDate;
}
