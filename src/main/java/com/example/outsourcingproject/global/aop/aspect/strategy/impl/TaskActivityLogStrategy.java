package com.example.outsourcingproject.global.aop.aspect.strategy.impl;

import com.example.outsourcingproject.domain.task.dto.TaskCreateResponseDto;
import com.example.outsourcingproject.domain.task.dto.TaskReadResponseDto;
import com.example.outsourcingproject.domain.task.enums.TaskStatus;
import com.example.outsourcingproject.global.aop.aspect.strategy.AbstractActivityLogStrategy;
import com.example.outsourcingproject.global.dto.ApiResponse;
import com.example.outsourcingproject.global.enums.ActivityType;
import com.example.outsourcingproject.global.enums.TargetType;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskActivityLogStrategy extends AbstractActivityLogStrategy {

    @Override
    public TargetType getTargetType() {
        return TargetType.TASK;
    }

    @Override
    public Long getTargetId() {
        if(activityLog.type().equals(ActivityType.TASK_CREATED)) {
            return getTaskIdFromResponseDto();
        } else {
            return getTaskIdFromRequestParams();
        }
    }

    @Override
    public String getMessage() {
        return extractTaskMessage();
    }

    private String extractTaskMessage() {
        if(activityLog.type().equals(ActivityType.TASK_STATUS_CHANGED)){
            return generateTaskStatusChangeMessage();
        }
        return activityLog.type().getMessage1();
    }

    private String generateTaskStatusChangeMessage() {
        if (
            response instanceof ResponseEntity<?> entity
                    && entity.getBody() instanceof ApiResponse<?> apiResponse
                    && apiResponse.getData() instanceof TaskReadResponseDto dto
        ) {
            if(dto.getStatus().equals(TaskStatus.IN_PROGRESS.toString())) {
                return String.format(ActivityType.TASK_STATUS_CHANGED.getMessage1(), TaskStatus.IN_PROGRESS);
            } else if(dto.getStatus().equals(TaskStatus.DONE.toString())) {
                return String.format(ActivityType.TASK_STATUS_CHANGED.getMessage1(), TaskStatus.DONE);
            }
        }
        return null;
    }

    private Long getTaskIdFromResponseDto() {
        if (
                response instanceof ResponseEntity<?> entity
                        && entity.getBody() instanceof ApiResponse<?> apiResponse
                        && apiResponse.getData() instanceof TaskCreateResponseDto dto
        ) {
            return dto.getId();
        }
        return null;
    }

    private Long getTaskIdFromRequestParams() {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameterNames.length; i++) {
            if ("taskId".equals(parameterNames[i]) && args[i] instanceof Long longArg) {
                return longArg;
            }
        }
        return null;
    }
}
