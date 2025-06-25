package com.example.outsourcingproject.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;



@Getter
@AllArgsConstructor(staticName = "of")
public class DailyTaskTrendDto {

    private LocalDate date;
    private long totalCount;
    private long doneCount;
}
