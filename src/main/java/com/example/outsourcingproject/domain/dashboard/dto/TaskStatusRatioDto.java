package com.example.outsourcingproject.domain.dashboard.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class TaskStatusRatioDto {
    private long todoCount;
    private long inProgressCount;
    private long doneCount;
}
