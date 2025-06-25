package com.example.outsourcingproject.domain.task.controller;

import com.example.outsourcingproject.domain.task.dto.*;
import com.example.outsourcingproject.domain.task.enums.TaskStatus;
import com.example.outsourcingproject.domain.task.service.TaskService;
import com.example.outsourcingproject.global.aop.annotation.ActivityLog;
import com.example.outsourcingproject.global.dto.ApiResponse;
import com.example.outsourcingproject.global.dto.PagedResponse;
import com.example.outsourcingproject.global.enums.ActivityType;
import com.example.outsourcingproject.global.enums.TargetType;
import com.example.outsourcingproject.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // Task 생성
    @ActivityLog(type = ActivityType.TASK_CREATED, target = TargetType.TASK)
    @PostMapping("/tasks")
    public ResponseEntity<ApiResponse<TaskCreateResponseDto>> createTask(@AuthenticationPrincipal UserPrincipal userPrincipal, @Valid @RequestBody TaskCreateRequestDto requestDto) {

        Long currentUserId = userPrincipal.getId();

        TaskCreateResponseDto responseDto = taskService.createTask(currentUserId, requestDto);

//        return new ResponseEntity<>(ApiResponse.success(responseDto), HttpStatus.CREATED);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDto, "Task 가 생성되었습니다."));

    }

    // Task - 전체 조회 -> 파라미터에 Status 값을 입력할 경우 해당 Status 의 Task만 조회
    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<PagedResponse<TaskReadResponseDto>>> getAllTasks(
            @RequestParam Optional<TaskStatus> status,
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size
    ) {
        PagedResponse<TaskReadResponseDto> getTasks = taskService.getTasksByStatus(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(getTasks, "Task 를 조회하였습니다."));
    }

    @GetMapping("/tasks/search/")
    public ResponseEntity<ApiResponse<PagedResponse<TaskReadResponseDto>>> searchByTitle(
            @RequestParam String query,
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size
    ) {
        PagedResponse<TaskReadResponseDto> searchTasks = taskService.searchTasksByTitle(query, page, size);
        return ResponseEntity.ok(ApiResponse.success(searchTasks, "[" + query + "] 제목이 포함된 Task 를 조회하였습니다."));
    }

    @GetMapping("/tasks/search/desc")
    public ResponseEntity<ApiResponse<PagedResponse<TaskReadResponseDto>>> searchByDescription(
            @RequestParam String searchText,
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size
    ) {
        PagedResponse<TaskReadResponseDto> searchTasks = taskService.searchTasksByDescription(searchText, page, size);
        return ResponseEntity.ok(ApiResponse.success(searchTasks, "[" + searchText + "] 내용이 포함된 Task 를 조회하였습니다."));
    }

    // Task - 단건 조회
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<TaskReadResponseDto>> getTaskById(@PathVariable Long taskId) {
        TaskReadResponseDto getTask = taskService.getTaskById(taskId);
        return ResponseEntity.ok(ApiResponse.success(getTask, "[ " + getTask.getTitle() + "] Task 를 조회하였습니다."));
    }

    // 특정 Task 수정 - 제목, 내용, 우선순위, 담당자, 마감일, 시작일
    @ActivityLog(type = ActivityType.TASK_UPDATED, target = TargetType.TASK)
    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<TaskReadResponseDto>> updateTask(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateRequestDto requestDto
            ) {

        Long currentUserId = userPrincipal.getId();  // 로그인된 유저의 ID
        TaskReadResponseDto updatedTask = taskService.updateTask(currentUserId, taskId, requestDto);

        return ResponseEntity.ok(ApiResponse.success(updatedTask, "Task 가 수정되었습니다."));
    }

    @ActivityLog(type = ActivityType.TASK_STATUS_CHANGED, target = TargetType.TASK)
    @PatchMapping("/tasks/{taskId}/status")
    public ResponseEntity<ApiResponse<TaskReadResponseDto>> updateStatusTask(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskStatusUpdateRequestDto requestDto
            ) {

        Long currentUserId = userPrincipal.getId();  // 로그인된 유저의 ID
        TaskReadResponseDto updateTaskStatus = taskService.updateTaskStatus(currentUserId, taskId, requestDto);

        return ResponseEntity.ok(ApiResponse.success(updateTaskStatus, "Task 상태가 [" + updateTaskStatus.getStatus() + "] (으)로 변경되었습니다."));
    }

    @ActivityLog(type = ActivityType.TASK_DELETED, target = TargetType.TASK)
    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<String>> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);

        return ResponseEntity.ok(ApiResponse.success("Task 가 삭제되었습니다."));

//        return ResponseEntity
//                .status(HttpStatus.NO_CONTENT)
//                .body(ApiResponse.success("Task 가 삭제되었습니다."));
    }
}
