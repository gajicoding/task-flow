package com.example.outsourcingproject.global.aop.aspect.strategy.impl;

import com.example.outsourcingproject.domain.auth.dto.response.SigninResponseDto;
import com.example.outsourcingproject.global.aop.aspect.strategy.AbstractActivityLogStrategy;
import com.example.outsourcingproject.global.aop.aspect.util.ActivityLogAspectUtil;
import com.example.outsourcingproject.global.dto.ApiResponse;
import com.example.outsourcingproject.global.enums.ActivityType;
import com.example.outsourcingproject.global.enums.TargetType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class UserActivityLogStrategy extends AbstractActivityLogStrategy {

    private Long userId = null;

    @Override
    public TargetType getTargetType() {
        return TargetType.USER;
    }

    @Override
    public Long getUserId() {
        if (activityLog.type().equals(ActivityType.USER_LOGGED_IN)) {
            // 로그인 시 응답 바디에서 id 값 가져오기
            userId = getUserIdFromResponseDto();
            return userId;
        } else {
            // UserPrincipal 에서 id 값 가져오기
            userId = ActivityLogAspectUtil.getUserIdFromUserPrincipal();
            return userId;
        }
    }

    @Override
    public Long getTargetId() {
        return userId;
    }


    private Long getUserIdFromResponseDto() {
        if (
                response instanceof ResponseEntity<?> entity
                        && entity.getBody() instanceof ApiResponse<?> apiResponse
                        && apiResponse.getData() instanceof SigninResponseDto dto
        ) {
            return dto.getUserId();
        }
        return null;
    }
}
