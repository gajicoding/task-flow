package com.example.outsourcingproject.domain.dashboard.service;

import com.example.outsourcingproject.domain.dashboard.dto.*;
import com.example.outsourcingproject.domain.dashboard.projection.DailyTaskTrendProjection;
import com.example.outsourcingproject.domain.dashboard.projection.MonthlyTaskTrendProjection;
import com.example.outsourcingproject.domain.dashboard.repository.AcitivityFeedRepository;
import com.example.outsourcingproject.domain.dashboard.repository.TaskStatisticsRepository;
import com.example.outsourcingproject.domain.task.enums.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @InjectMocks
    private DashboardService dashboardService;

    @Mock
    private TaskStatisticsRepository taskStatisticsRepository;
    @Mock
    private AcitivityFeedRepository acitivityFeedRepository;

    @Test
    @DisplayName("주간 통계가 올바르게 계산된다")
    void getWeeklyStatistics_success() {
        // given
        LocalDate date = LocalDate.of(2025, 6, 18); // 수요일
        LocalDate monday = date.with(DayOfWeek.MONDAY);
        LocalDateTime weekStart = monday.atStartOfDay();
        LocalDateTime weekEnd = monday.plusWeeks(1).atStartOfDay();
        LocalDateTime prevWeekStart = monday.minusWeeks(1).atStartOfDay();
        LocalDateTime prevWeekEnd = weekStart;

        when(taskStatisticsRepository.countByCreatedAtBetween(weekStart, weekEnd)).thenReturn(10L);
        when(taskStatisticsRepository.countByStatusAndPeriod(TaskStatus.TODO, weekStart, weekEnd)).thenReturn(3L);
        when(taskStatisticsRepository.countByStatusAndPeriod(TaskStatus.IN_PROGRESS, weekStart, weekEnd)).thenReturn(4L);
        when(taskStatisticsRepository.countByStatusAndPeriod(TaskStatus.DONE, weekStart, weekEnd)).thenReturn(3L);
        when(taskStatisticsRepository.countOverdueTasksInPeriod(
                anyList(),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(2L);
        when(taskStatisticsRepository.countByCreatedAtBetween(prevWeekStart, prevWeekEnd)).thenReturn(5L);

        // when
        DashboardStatisticsDto dto = dashboardService.getWeeklyStatistics(date);

        //then
        assertThat(dto.getTotalCount()).isEqualTo(10);
        assertThat(dto.getWeeklyChangeRate()).isEqualTo(100.0);
        assertThat(dto.getCompletionRate()).isEqualTo(30.0);
        assertThat(dto.getInProgressRate()).isEqualTo(40.0);
        assertThat(dto.getOverdueRate()).isEqualTo(20.0);

        verify(taskStatisticsRepository).countByCreatedAtBetween(weekStart, weekEnd);
    }

    @Test
    @DisplayName("오늘 태스크 조회시 repository가 올바른 파라미터로 호출된다")
    void getMyTodayTasks_callsRepositoryCorrectly() {
        // given
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.toLocalDate().plusDays(1).atStartOfDay();
        Pageable topFive = PageRequest.of(0, 5);

        List<TodayTaskItemDto> expected = List.of(
                TodayTaskItemDto.from(1L, "T1", TaskStatus.TODO, null, end.minusHours(3)),
                TodayTaskItemDto.from(2L, "T2", TaskStatus.IN_PROGRESS, null, end.minusHours(2))
        );

        when(taskStatisticsRepository.findTodayTasks(eq(userId),
                eq(List.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS)), eq(now), eq(end), eq(topFive)))
                .thenReturn(expected);

        // when
        List<TodayTaskItemDto> result = dashboardService.getMyTodayTasks(userId);

        //then
        assertThat(result).containsExactlyElementsOf(expected);
    }

    @Test
    @DisplayName("주간 트렌드 변환이 Projection을 DTO로 매핑한다")
    void weeklyTrend_mapsProjection() {
        // given
        LocalDate today = LocalDate.of(2025, 6, 18);
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDateTime start = monday.atStartOfDay();
        LocalDateTime end = monday.plusWeeks(1).atStartOfDay();

        DailyTaskTrendProjection p1 = projection(LocalDate.of(2025, 6, 16), 4, 2);
        DailyTaskTrendProjection p2 = projection(LocalDate.of(2025, 6, 17), 3, 1);

        when(taskStatisticsRepository.fetchDailyTrend(start, end)).thenReturn(List.of(p1, p2));

        // when
        List<DailyTaskTrendDto> dtos = dashboardService.getWeeklyTrend(today);

        //then
        assertThat(dtos).hasSize(2)
                .extracting(DailyTaskTrendDto::getTotalCount)
                .containsExactly(4L, 3L);
    }

    @Test
    @DisplayName("상태 비율 계산 DTO 반환")
    void statusRatio_success() {
        // given
        when(taskStatisticsRepository.countGroupByStatus()).thenReturn(List.of(
                TaskStatusCountDto.of(TaskStatus.TODO, 2),
                TaskStatusCountDto.of(TaskStatus.IN_PROGRESS, 3),
                TaskStatusCountDto.of(TaskStatus.DONE, 5)
        ));

        // when
        TaskStatusRatioDto dto = dashboardService.getStatusRatio();

        //then
        assertThat(dto.getTodoCount()).isEqualTo(2);
        assertThat(dto.getInProgressCount()).isEqualTo(3);
        assertThat(dto.getDoneCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("진행률 계산")
    void progressRatio_success() {
        // given
        Long userId = 1L;
        when(taskStatisticsRepository.countMyStatus(userId)).thenReturn(List.of(
                TaskStatusCountDto.of(TaskStatus.TODO, 1),
                TaskStatusCountDto.of(TaskStatus.IN_PROGRESS, 1),
                TaskStatusCountDto.of(TaskStatus.DONE, 2)
        ));
        when(taskStatisticsRepository.countTeamStatus()).thenReturn(List.of(
                TaskStatusCountDto.of(TaskStatus.TODO, 3),
                TaskStatusCountDto.of(TaskStatus.IN_PROGRESS, 2),
                TaskStatusCountDto.of(TaskStatus.DONE, 5)
        ));

        // when
        ProgressRatioDto dto = dashboardService.getProgressRatio(userId);

        //then
        assertThat(dto.getMyRate()).isEqualTo(50.0); // 2/4
        assertThat(dto.getTeamRate()).isEqualTo(50.0); // 5/10
    }

    @Test
    @DisplayName("월간 트렌드는 12개의 DTO를 리턴한다")
    void monthlyTrend_success() {
        // given
        int year = 2025;
        List<MonthlyTaskTrendProjection> projections = List.of(
                monthlyProjection(1, 2, 1),
                monthlyProjection(6, 5, 3)
        );

        when(taskStatisticsRepository.fetchFixedMonthlyTrend(year)).thenReturn(projections);

        // when
        List<MonthlyTaskTrendDto> result = dashboardService.getMonthlyTrend();

        // then
        assertThat(result).hasSize(12);
        assertThat(result.get(0).getTotalCount()).isEqualTo(2);
        assertThat(result.get(5).getTotalCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("활동 피드 조회")
    void activityFeed_success() {
        // given
        List<ActivityFeedDto> feeds = List.of(
                ActivityFeedDto.of("user", "메시지", LocalDateTime.now())
        );
        when(acitivityFeedRepository.fetchFeed(any(), any(), any(Pageable.class))).thenReturn(feeds);

        // when
        List<ActivityFeedDto> result = dashboardService.getAcitivityFeed();

        //then
        assertThat(result).isEqualTo(feeds);
    }

    /* util methods */

    private DailyTaskTrendProjection projection(LocalDate date, long total, long done) {
        return new DailyTaskTrendProjection() {
            @Override public LocalDate getDate() { return date; }
            @Override public long getTotalCount() { return total; }
            @Override public long getDoneCount() { return done; }
        };
    }
    private MonthlyTaskTrendProjection monthlyProjection(int month, long total, long done) {
        return new MonthlyTaskTrendProjection() {
            @Override public int getMonth() { return month; }
            @Override public long getTotalCount() { return total; }
            @Override public long getDoneCount() { return done; }
        };
    }
}
