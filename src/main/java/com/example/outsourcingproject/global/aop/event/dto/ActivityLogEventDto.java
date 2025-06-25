package com.example.outsourcingproject.global.aop.event.dto;

import com.example.outsourcingproject.global.enums.ActivityType;
import com.example.outsourcingproject.global.enums.RequestMethod;
import com.example.outsourcingproject.global.enums.TargetType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ActivityLogEventDto {
    private final Long userId;
    private final ActivityType activityType;
    private final TargetType targetType;
    private final Long TargetId;
    private final String message;
    private final String requestIp;
    private final RequestMethod requestMethod;
    private final String requestUrl;
}
