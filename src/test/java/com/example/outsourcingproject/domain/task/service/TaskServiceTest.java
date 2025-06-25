package com.example.outsourcingproject.domain.task.service;

import com.example.outsourcingproject.domain.task.dto.TaskCreateRequestDto;
import com.example.outsourcingproject.domain.task.dto.TaskCreateResponseDto;
import com.example.outsourcingproject.domain.task.entity.Task;
import com.example.outsourcingproject.domain.task.enums.Priority;
import com.example.outsourcingproject.domain.task.enums.TaskErrorCode;
import com.example.outsourcingproject.domain.task.exception.TaskException;
import com.example.outsourcingproject.domain.task.repository.TaskRepository;
import com.example.outsourcingproject.domain.user.entity.User;
import com.example.outsourcingproject.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("태스크 생성 - 정상적인 요청으로 TASK 생성 성공 테스트")
    void createTask_success_whenValidRequest() {

        //given
        Long userId = 1L;
        String assigneeName = "AssigneeUser";


        User creator = new User(userId, "CreatorUser", "creator@test.com");
        User assignee = new User(2L, assigneeName, "assignee@test.com");

        TaskCreateRequestDto requestDto = new TaskCreateRequestDto("New Task" , "Test Description", Priority.HIGH, assigneeName, LocalDateTime.now().plusDays(1), LocalDateTime.now());


        // userRepository.findById 호출 -> 로그인 유저 반환
        when(userRepository.findById(userId)).thenReturn(Optional.of(creator));
        // assignee 유저 조회를 위해 전체 유저 리스트 조회 -> assigneeName 과 일치하는 유저 찾기
        when(userRepository.findAll()).thenReturn(List.of(creator, assignee));
        // save() 호출 -> 실제 저장된 Task 객체 그대로 반환
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        TaskCreateResponseDto response = taskService.createTask(userId, requestDto);

        //then
        assertThat(response.getTitle()).isEqualTo("New Task");
        assertThat(response.getPriority()).isEqualTo("HIGH");
        assertThat(response.getAssignee().getName()).isEqualTo("AssigneeUser");
        assertThat(response.getCreator()).isEqualTo("CreatorUser");

    }

    @Test
    @DisplayName("존재하지 않는 로그인 유저 ID(탈퇴 등) 로 인한 Task 생성 실패 테스트")
    void createTask_fail_whenUserNotFound() {

        // given
        Long userId = 999999999L; // 없는 ID
        TaskCreateRequestDto requestDto = new TaskCreateRequestDto(
                "Test Task",
                "Description 123",
                Priority.MEDIUM,
                "AssigneeUser",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now()
        );

        // 로그인한 유저가 없다?
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> taskService.createTask(userId, requestDto))
                .isInstanceOf(TaskException.class)
                .hasMessageContaining(TaskErrorCode.TASK_CREATOR_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("AssigneeName 이 null 일 경우 기본값으로 Creator 로 저장 -> Task 생성 성공 테스트")
    void createTask_success_whenAssigneeNameIsBlank_thenDefaultToCreator() {

        //given
        Long userId = 1L;
        String creatorName = "CreatorUser";

        User creator = new User(userId, creatorName, "creator@test.com");

        TaskCreateRequestDto requestDto = new TaskCreateRequestDto(
                "Default Assignee",
                "Assignee is null",
                Priority.MEDIUM,
                null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(creator));
        when(userRepository.findAll()).thenReturn(List.of(creator));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        TaskCreateResponseDto response = taskService.createTask(userId, requestDto);

        //then
        assertThat(response.getAssignee().getName()).isEqualTo(creatorName);
        assertThat(response.getCreator()).isEqualTo(creatorName);

    }

    @Test
    @DisplayName("AssigneeName 을 가진 유저가 존재하지 않을 경우 예외 처리 테스트")
    void createTask_fail_whenAssigneeNameNotFound() {

        // given
        Long userId = 1L;
        String NotFoundName = "NotFoundName";

        User creator = new User(userId, "CreatorUser", "creator@test.com");

        TaskCreateRequestDto requestDto = new TaskCreateRequestDto(
                "Not Found Assignee",
                "Assignee does not exist",
                Priority.MEDIUM,
                NotFoundName,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(creator));
        when(userRepository.findAll()).thenReturn(List.of(creator));

        // when & then
        assertThatThrownBy(() -> taskService.createTask(userId, requestDto))
                .isInstanceOf(TaskException.class)
                .hasMessageContaining(TaskErrorCode.USER_NOT_FOUND.getMessage());
    }
}