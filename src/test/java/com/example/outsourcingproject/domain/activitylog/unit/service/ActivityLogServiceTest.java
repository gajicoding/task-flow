package com.example.outsourcingproject.domain.activitylog.unit.service;

import com.example.outsourcingproject.domain.activitylog.controller.dto.ActivityLogResponseDto;
import com.example.outsourcingproject.domain.activitylog.domain.model.ActivityLog;
import com.example.outsourcingproject.domain.activitylog.domain.repository.ActivityLogRepository;
import com.example.outsourcingproject.domain.activitylog.service.ActivityLogService;
import com.example.outsourcingproject.domain.activitylog.service.dto.FindAllOptionDto;
import com.example.outsourcingproject.domain.auth.exception.UserNotFoundException;
import com.example.outsourcingproject.domain.user.entity.User;
import com.example.outsourcingproject.domain.user.repository.UserRepository;
import com.example.outsourcingproject.global.dto.PagedResponse;
import com.example.outsourcingproject.global.enums.ActivityType;
import com.example.outsourcingproject.global.enums.TargetType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ActivityLogServiceTest {

    @Mock
    ActivityLogRepository activityLogRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    private ActivityLogService activityLogService;

    @Test
    void 로그_전체_조회_성공() {
        // given
        FindAllOptionDto option = new FindAllOptionDto(
                PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt"),
                1L,
                null,
                null,
                null,
                null
        );

        // 유저가 존재하는 경우
        User user = new User(
                1L,
                "홍길동",
                "test@gmail.com"
        );
        given(userRepository.findById(option.getUserId())).willReturn(Optional.of(user));

        Page<ActivityLog> page = Page.empty(option.getPageable());
        given(activityLogRepository.findActivityLogs(any(), any(), any(), any(), any(), any()))
                .willReturn(page);

        // when
        activityLogService.findAll(option);

        // then
        verify(activityLogRepository, times(1)).findActivityLogs(
                any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void 사용자_ID가_존재하지_않으면_예외가_발생한다() {
        FindAllOptionDto option = new FindAllOptionDto(
                PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt"),
                1L,
                null,
                null,
                null,
                null
        );

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // when
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            activityLogService.findAll(option);
        });

        // then
        assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());
    }
}
