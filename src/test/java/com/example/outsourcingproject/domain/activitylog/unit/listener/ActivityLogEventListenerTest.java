package com.example.outsourcingproject.domain.activitylog.unit.listener;

import com.example.outsourcingproject.domain.activitylog.domain.model.ActivityLog;
import com.example.outsourcingproject.domain.activitylog.domain.repository.ActivityLogRepository;
import com.example.outsourcingproject.domain.activitylog.listener.ActivityLogEventListener;
import com.example.outsourcingproject.domain.auth.entity.Auth;
import com.example.outsourcingproject.domain.auth.enums.UserRole;
import com.example.outsourcingproject.domain.auth.repository.AuthRepository;
import com.example.outsourcingproject.global.aop.event.dto.ActivityLogEventDto;
import com.example.outsourcingproject.global.enums.ActivityType;
import com.example.outsourcingproject.global.enums.RequestMethod;
import com.example.outsourcingproject.global.enums.TargetType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ActivityLogEventListenerTest {

    @Mock
    AuthRepository authRepository;

    @Mock
    ActivityLogRepository activityLogRepository;

    @InjectMocks
    ActivityLogEventListener listener;

    @Test
    void ActivityLogEventListener_정상동작() {
        // given
        Long userId = 1L;
        ActivityLogEventDto event = new ActivityLogEventDto(
                userId,
                ActivityType.USER_LOGGED_IN,
                TargetType.USER,
                userId,
                ActivityType.USER_LOGGED_IN.getMessage1(),
                "127.0.0.1",
                RequestMethod.GET,
                "/api/auth/login"
        );

        Auth auth = new Auth("홍길동", "test1234", "test@gmail.com", "test1234", UserRole.USER);

        when(authRepository.findById(userId)).thenReturn(Optional.of(auth));
        when(activityLogRepository.save(any(ActivityLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        listener.handleActivityLogEvent(event);

        // then
        verify(authRepository).findById(userId);
        verify(activityLogRepository).save(any(ActivityLog.class));
    }
}
