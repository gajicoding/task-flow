package com.example.outsourcingproject.global.aop.aspect.strategy;

import com.example.outsourcingproject.global.aop.annotation.ActivityLog;
import com.example.outsourcingproject.global.enums.TargetType;
import org.aspectj.lang.ProceedingJoinPoint;

public interface ActivityLogStrategy {

    void initialize(ProceedingJoinPoint joinPoint, ActivityLog activityLog, Object response);

    TargetType getTargetType();

    Long getUserId();

    Long getTargetId();

    String getMessage();
}
