package com.example.outsourcingproject.domain.task.dto;

import com.example.outsourcingproject.domain.task.entity.Task;
import com.example.outsourcingproject.domain.task.enums.Priority;
import com.example.outsourcingproject.domain.task.enums.TaskStatus;
import com.example.outsourcingproject.domain.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TaskReadResponseDto {

    private Long id;

    private String title;

    private String description;

    private String status;

    private LocalDateTime dueDate;

    private String priority;

//    private String assignee;
    private UserSummaryDto assignee;

    private String creator;

    private LocalDateTime startedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public TaskReadResponseDto(Long id, String title, String description, TaskStatus status, LocalDateTime deadline, Priority priority, UserSummaryDto assignee, String creator, LocalDateTime startedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status.name();
        this.dueDate = deadline;
        this.priority = priority.name();
        this.assignee = assignee;
        this.creator = creator;
        this.startedAt = startedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TaskReadResponseDto toDto(Task task) {
        return new TaskReadResponseDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDeadline(),
                task.getPriority(),
                new UserSummaryDto(task.getAssignee()),
                task.getCreator().getName(),
                task.getStartedAt(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
