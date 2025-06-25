package com.example.outsourcingproject.global.aop.aspect.strategy.impl;

import com.example.outsourcingproject.domain.comment.dto.CommentResponseDto;
import com.example.outsourcingproject.global.aop.aspect.strategy.AbstractActivityLogStrategy;
import com.example.outsourcingproject.global.dto.ApiResponse;
import com.example.outsourcingproject.global.enums.ActivityType;
import com.example.outsourcingproject.global.enums.TargetType;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class CommentActivityLogStrategy extends AbstractActivityLogStrategy {

    @Override
    public TargetType getTargetType() {
        return TargetType.COMMENT;
    }

    @Override
    public Long getTargetId() {
        if(activityLog.type().equals(ActivityType.COMMENT_CREATED)) {
            return getCommentFromResponseDto();
        } else {
            return getCommentIdFromRequestParams();
        }
    }

    private Long getCommentFromResponseDto() {
        if (
                response instanceof ResponseEntity<?> entity
                        && entity.getBody() instanceof ApiResponse<?> apiResponse
                        && apiResponse.getData() instanceof CommentResponseDto dto
        ) {
            return dto.getId();
        }
        return null;
    }

    private Long getCommentIdFromRequestParams() {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameterNames.length; i++) {
            if ("commentId".equals(parameterNames[i]) && args[i] instanceof Long longArg) {
                return longArg;
            }
        }
        return null;
    }
}
