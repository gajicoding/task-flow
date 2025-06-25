package com.example.outsourcingproject.domain.user.service;

import com.example.outsourcingproject.domain.auth.exception.UserNotFoundException;
import com.example.outsourcingproject.domain.auth.repository.AuthRepository;
import com.example.outsourcingproject.domain.user.dto.UserResponseDto;
import com.example.outsourcingproject.domain.user.dto.request.UserInfoResponseDto;
import com.example.outsourcingproject.domain.user.entity.User;
import com.example.outsourcingproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;

    @Transactional(readOnly = true)
    public UserInfoResponseDto getUserInfo(Long userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자 프로필을 찾을 수 없습니다."));

        return new UserInfoResponseDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getUserAll() {
        return authRepository.findAll().stream().map(UserResponseDto::new).toList();
    }
}
