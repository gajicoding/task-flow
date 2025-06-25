package com.example.outsourcingproject.domain.task.dto;

import com.example.outsourcingproject.domain.task.entity.Task;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TaskCreateResponseDto {

    private Long id;

    private String title;

    private String description;

    private String status;

    private LocalDateTime deadline;

    private String priority;

//    private String assigneeName;
    private UserSummaryDto assignee;

    private String creator;

    private LocalDateTime startedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public TaskCreateResponseDto(Long id, String title, String description,
                                 String status,
                                 LocalDateTime deadline, String priority, UserSummaryDto assignee,
                                 String creator,
                                 LocalDateTime startedAt, LocalDateTime createdAt,
                                 LocalDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.deadline = deadline;
        this.priority = priority;
        this.assignee = assignee;
        this.creator = creator;
        this.startedAt = startedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TaskCreateResponseDto toDto(Task task) {
        return new TaskCreateResponseDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                task.getDeadline(),
                task.getPriority().name(),
                new UserSummaryDto(task.getAssignee()),
                task.getCreator().getName(),
                task.getStartedAt(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
