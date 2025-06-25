package com.example.outsourcingproject.domain.activitylog.unit.repository;

import com.example.outsourcingproject.domain.activitylog.domain.model.ActivityLog;
import com.example.outsourcingproject.domain.activitylog.domain.repository.ActivityLogRepository;
import com.example.outsourcingproject.domain.auth.entity.Auth;
import com.example.outsourcingproject.domain.auth.enums.UserRole;
import com.example.outsourcingproject.domain.auth.repository.AuthRepository;
import com.example.outsourcingproject.global.enums.ActivityType;
import com.example.outsourcingproject.global.enums.RequestMethod;
import com.example.outsourcingproject.global.enums.TargetType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@DataJpaTest
@EnableJpaAuditing
public class ActivityLogRepositoryTest {

    @Autowired
    ActivityLogRepository activityLogRepository;

    @Autowired
    AuthRepository authRepository;

    @Test
    void 로그_저장_테스트() {

        // given
        Auth auth = new Auth("홍길동", "test1234", "test@gmail.com", "test1234", UserRole.USER);
        auth = authRepository.save(auth);

        ActivityLog activityLog = new ActivityLog(
                auth,
                ActivityType.USER_LOGGED_IN,
                TargetType.USER,
                auth.getId(),
                "로그인 완료",
                "127.0.0.1",
                RequestMethod.POST,
                "/api/v1/auth/login"
        );

        // when
        ActivityLog savedActivityLog = activityLogRepository.save(activityLog);

        //then
        Assertions.assertThat(savedActivityLog.getId()).isNotNull();
    }
}
