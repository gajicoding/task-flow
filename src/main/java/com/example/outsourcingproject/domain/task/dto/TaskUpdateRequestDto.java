package com.example.outsourcingproject.domain.task.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Task 수정 요청 DTO
 * - 제목, 내용, 우선순위, 담당자, 마감일, 시작일
 */
@Getter
public class TaskUpdateRequestDto {

    private String title;
    private String description;
    private String priority;
    private String assigneeName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;
}

