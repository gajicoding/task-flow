package com.example.outsourcingproject.domain.dashboard.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class DashboardStatisticsDto {
    private long totalCount;         // 전체 태스크 수
    private double weeklyChangeRate; // 전체 작업 수 증가율 (%)
    private long doneCount;          // DONE 태스크 수
    private double completionRate;   // 완료율 (%)
    private long inProgressCount;    // IN_PROGRESS 태스크 수
    private double inProgressRate;   // 진행률 (%)
    private long todoCount;          // TODO 태스크 수
    private long overdueCount;       // 기한 초과 태스크 수
    private double overdueRate;      // 지연율 (%)
}
