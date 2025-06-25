package com.example.outsourcingproject.domain.user.service;

import com.example.outsourcingproject.domain.auth.event.UserRegisteredEvent;
import com.example.outsourcingproject.domain.auth.event.UserWithdrawnEvent;
import com.example.outsourcingproject.domain.user.entity.User;
import com.example.outsourcingproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventListener {

    private final UserRepository userRepository;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserRegistered(UserRegisteredEvent event) {

        log.info("사용자 등록 이벤트 처리 시작: userId={}, name={}", event.getUserId(), event.getName());

        try {
            User user = new User(event.getUserId(), event.getName(), event.getEmail());
            userRepository.save(user);

            log.info("사용자 프로필 생성 완료: userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("사용자 프로필 생성 실패: userId={}, name={}, error={}",
                    event.getUserId(), event.getName(), e.getMessage(), e);

            throw e;
        }
    }

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserWithdrawn(UserWithdrawnEvent event) {

        log.info("사용자 탈퇴 이벤트 처리 시작: userId={}", event.getUserId());

        try {
            userRepository.findByIdAndIsDeletedFalse(event.getUserId())
                    .ifPresentOrElse(user -> {
                        user.delete();
                        userRepository.save(user);
                        log.info("사용자 프로필 삭제 완료: userId={}", event.getUserId());
                    },
                            () -> log.warn("삭제할 사용자 프로필을 찾을 수 없습니다. : userId={}", event.getUserId())
                    );
    } catch (Exception e) {
        log.error("사용자 프로필 삭제 실패: userId{}, error={}",
                event.getUserId(), e.getMessage());

        throw e;
        }
    }
}
