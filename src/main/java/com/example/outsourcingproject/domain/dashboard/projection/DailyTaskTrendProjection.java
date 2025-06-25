package com.example.outsourcingproject.domain.dashboard.projection;

import java.time.LocalDate;

public interface DailyTaskTrendProjection {
    LocalDate getDate();
    long getTotalCount();
    long getDoneCount();
}
