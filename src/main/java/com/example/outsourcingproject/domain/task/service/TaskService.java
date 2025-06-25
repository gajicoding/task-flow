package com.example.outsourcingproject.domain.task.service;

import com.example.outsourcingproject.domain.task.dto.*;
import com.example.outsourcingproject.domain.task.entity.Task;
import com.example.outsourcingproject.domain.task.enums.Priority;
import com.example.outsourcingproject.domain.task.enums.TaskErrorCode;
import com.example.outsourcingproject.domain.task.enums.TaskStatus;
import com.example.outsourcingproject.domain.task.exception.TaskException;
import com.example.outsourcingproject.domain.task.repository.TaskRepository;
import com.example.outsourcingproject.domain.user.entity.User;
import com.example.outsourcingproject.domain.user.repository.UserRepository;
import com.example.outsourcingproject.global.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;


    // Task 생성
    @Transactional
    public TaskCreateResponseDto createTask (Long currentUserId, TaskCreateRequestDto requestDto) {

        // Task 생성자 = 로그인 유저 검증
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(()-> new TaskException(TaskErrorCode.TASK_CREATOR_NOT_FOUND));

        // 요청 데이터에서 assigneeName 값이 없을 경우 기본값으로 creator 로 처리
        String assigneeName = Optional.ofNullable(requestDto.getAssigneeName())
                .filter(name -> !name.isBlank())
                .orElse(creator.getName());

        // assigneeName 으로 유저 정보 찾기
        User assignee = findUserName(assigneeName);

        String title = Optional.ofNullable(requestDto.getTitle()).filter(t -> !t.trim().isEmpty()).orElseThrow(() -> new TaskException(TaskErrorCode.INVALID_TASK_TITLE));


        // priority 값 검증
        Priority priority = Optional.ofNullable(requestDto.getPriority())
                .map(p -> {
                    try {
                        return Priority.valueOf(p); // Enum 값이 유효한지 확인
                    } catch (IllegalArgumentException e) {
                        throw new TaskException(TaskErrorCode.INVALID_TASK_PRIORITY); // 잘못된 값일 경우 예외 처리
                    }
                })
                .orElse(Priority.MEDIUM);  // null 일 경우

        TaskStatus taskStatus = TaskStatus.TODO;


        Task newTask = new Task(
                title,
                requestDto.getDescription(),
                priority,
                assignee,
                creator,
                taskStatus,
                requestDto.getDeadline(),
                requestDto.getStartedAt()
        );

        taskRepository.save((newTask));

        return TaskCreateResponseDto.toDto(newTask);
    }

    // Task 조회 - 전체
    public PagedResponse<TaskReadResponseDto> getTasksByStatus(Optional<TaskStatus> status, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Task> tasks;

        // Status 값이 있을 경우 해당 Status 에 맞는 Task 조회
        if (status.isPresent()) {
            tasks = taskRepository.findAllByIsDeletedFalseAndStatus(status.get(), pageable);
        } else { // Status 값이 없을 경우 모든 Task 조회
            tasks = taskRepository.findAllByIsDeletedFalse(pageable);
        }

        // Page<TaskReadResponseDto> 타입으로 변환
        Page<TaskReadResponseDto> result = tasks.map(TaskReadResponseDto::toDto);

        return PagedResponse.toPagedResponse(result);
    }

    // Task 조회 - 제목(title) 검색
    public PagedResponse<TaskReadResponseDto> searchTasksByTitle(String searchText, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Task> searchTask = taskRepository.findByTitleContaining(searchText, pageable);

        // Page<TaskReadResponseDto> 타입으로 변환
        Page<TaskReadResponseDto> result = searchTask.map(TaskReadResponseDto::toDto);

        return PagedResponse.toPagedResponse(result);
    }

    // Task 조회 - 내용(description) 검색
    public PagedResponse<TaskReadResponseDto> searchTasksByDescription(String searchText, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Task> searchTask = taskRepository.findByDescriptionContaining(searchText, pageable);

        // Page<TaskReadResponseDto> 타입으로 변환
        Page<TaskReadResponseDto> result = searchTask.map(TaskReadResponseDto::toDto);

        return PagedResponse.toPagedResponse(result);
    }


    // Task 조회 - 단 건
    public TaskReadResponseDto getTaskById(Long taskId) {
        Task task = findTaskById(taskId);

        return TaskReadResponseDto.toDto(task);
    }


    // 특정 Task 수정 - 내용 수정
    @Transactional
    public TaskReadResponseDto updateTask(Long currentUserId, Long taskId, TaskUpdateRequestDto requestDto) {
        Task task = findTaskById(taskId);

        User assignee = Optional.ofNullable(requestDto.getAssigneeName())
                .map(this::findUserName)
                .orElse(task.getAssignee());

        task.updateTask(
                requestDto.getTitle(),
                requestDto.getDescription(),
                requestDto.getPriority() != null ? Priority.fromString(requestDto.getPriority()) : null,
                assignee,
                requestDto.getDeadline(),
                requestDto.getStartedAt());

        taskRepository.save(task);

        return TaskReadResponseDto.toDto(task);
    }

    // Task 상태 수정
    @Transactional
    public TaskReadResponseDto updateTaskStatus(Long currentUserId, Long taskId, TaskStatusUpdateRequestDto requestDto) {
        Task task = findTaskById(taskId);

        // 로그인 유저의 권한 확인
//        if (!isUserAdmin(currentUserId)) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자 권한을 보유한 유저만 이용할 수 있습니다.");
//        }

        TaskStatus updateStatus = TaskStatus.fromString(requestDto.getStatus());

        // 수정한 상태 저장
        task.setStatus(updateStatus);
        taskRepository.save(task);

        return TaskReadResponseDto.toDto(task);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        // Task 조회
        Task task = findTaskById(taskId);

        // Soft Delete
        task.delete();

        // task 객체 저장 -> 변경 사항 DB 반영
        taskRepository.save(task);
    }


    // name 으로 User 찾기
    private User findUserName(String assigneeName) {
        return userRepository.findAll().stream()
                .filter(user -> user.getName().equals(assigneeName))
                .findFirst()
                .orElseThrow(() -> new TaskException(TaskErrorCode.USER_NOT_FOUND));
    }

    // taskId 로 Task 찾기
    private Task findTaskById(Long taskId) {
        return taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new TaskException(TaskErrorCode.TASK_NOT_FOUND));
    }

    // 유저가 보유한 권한 확인 (Admin? User?)
//    private boolean isUserAdmin(Long userId) {
//        User user = userRepository.findById(userId).orElseThrow(() -> new TaskException(TaskErrorCode.USER_NOT_FOUND));
//        return user.getRole().equals(Role.ADMIN);
//    }

}
