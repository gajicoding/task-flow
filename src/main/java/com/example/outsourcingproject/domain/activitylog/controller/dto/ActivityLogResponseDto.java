package com.example.outsourcingproject.domain.activitylog.controller.dto;

import com.example.outsourcingproject.domain.activitylog.domain.model.ActivityLog;
import com.example.outsourcingproject.domain.user.dto.UserResponseDto;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ActivityLogResponseDto {
    private final Long id;
    private final String type;
    private final Long userId;
    private final UserResponseDto user;
    private final int taskId;
    private final LocalDateTime timestamp;
    private final String description;

    public ActivityLogResponseDto(ActivityLog activityLog) {
        this.id = activityLog.getId();
        this.type = activityLog.getActivityType().toString();
        this.userId = activityLog.getAuth().getId();
        this.user = new UserResponseDto(activityLog.getAuth());
        this.taskId = activityLog.getTargetType().getId();
        this.timestamp = activityLog.getCreatedAt();
        this.description = activityLog.getMessage();
    }
}
