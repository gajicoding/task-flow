package com.example.outsourcingproject.domain.task.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * Task 상태 수정 요청 DTO
 */
@Getter
public class TaskStatusUpdateRequestDto {

    @NotNull(message = "변경할 작업 상태를 입력해주세요")
    private String status;
}
