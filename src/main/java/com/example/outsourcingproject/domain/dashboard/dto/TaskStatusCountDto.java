package com.example.outsourcingproject.domain.dashboard.dto;

import com.example.outsourcingproject.domain.task.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class TaskStatusCountDto {

    private TaskStatus status;
    private long count;
}
