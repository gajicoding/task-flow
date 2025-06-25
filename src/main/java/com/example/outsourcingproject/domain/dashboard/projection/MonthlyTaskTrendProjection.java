package com.example.outsourcingproject.domain.dashboard.projection;

public interface MonthlyTaskTrendProjection {
    int getMonth();
    long getTotalCount();
    long getDoneCount();
}