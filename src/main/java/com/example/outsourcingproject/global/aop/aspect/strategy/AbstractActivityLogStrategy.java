package com.example.outsourcingproject.global.aop.aspect.strategy;

import com.example.outsourcingproject.global.aop.annotation.ActivityLog;
import com.example.outsourcingproject.global.aop.aspect.util.ActivityLogAspectUtil;
import org.aspectj.lang.ProceedingJoinPoint;

public abstract class AbstractActivityLogStrategy implements ActivityLogStrategy{

    protected ProceedingJoinPoint joinPoint;
    protected ActivityLog activityLog;
    protected Object response;

    @Override
    public void initialize(ProceedingJoinPoint joinPoint, ActivityLog activityLog, Object response) {
        this.joinPoint = joinPoint;
        this.activityLog = activityLog;
        this.response = response;
    }

    public Long getUserId() {
        return ActivityLogAspectUtil.getUserIdFromUserPrincipal();
    }

    @Override
    public String getMessage() {
        return activityLog.type().getMessage1();
    }
}
