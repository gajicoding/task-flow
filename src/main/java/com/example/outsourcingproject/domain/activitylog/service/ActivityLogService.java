package com.example.outsourcingproject.domain.activitylog.service;

import com.example.outsourcingproject.domain.activitylog.controller.dto.*;
import com.example.outsourcingproject.domain.activitylog.domain.repository.ActivityLogRepository;
import com.example.outsourcingproject.domain.activitylog.service.dto.FindAllOptionDto;
import com.example.outsourcingproject.domain.auth.exception.UserNotFoundException;
import com.example.outsourcingproject.domain.user.repository.UserRepository;
import com.example.outsourcingproject.global.dto.PagedResponse;
import com.example.outsourcingproject.global.enums.ActivityType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    public PagedResponse<ActivityLogResponseDto> findAll(FindAllOptionDto option) {

        if (option.getUserId() != null) {
            userRepository.findById(option.getUserId()).orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        }

        ActivityType activityType = ActivityType.of(option.getActivityType()).orElse(null);

        LocalDateTime startDateTime = option.getStartDate() != null ? option.getStartDate().atStartOfDay() : null;
        LocalDateTime endDateTime = option.getEndDate() != null ? option.getEndDate().atTime(LocalTime.MAX) : null;

        Page<ActivityLogResponseDto> logPages = activityLogRepository.findActivityLogs(
                option.getUserId(),
                activityType,
                option.getTaskId(),
                startDateTime,
                endDateTime,
                option.getPageable()
        ).map(ActivityLogResponseDto::new);

        return PagedResponse.toPagedResponse(logPages);
    }
}
