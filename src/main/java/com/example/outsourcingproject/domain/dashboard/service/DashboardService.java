package com.example.outsourcingproject.domain.dashboard.service;

import com.example.outsourcingproject.domain.dashboard.dto.*;
import com.example.outsourcingproject.domain.dashboard.projection.DailyTaskTrendProjection;
import com.example.outsourcingproject.domain.dashboard.repository.AcitivityFeedRepository;
import com.example.outsourcingproject.domain.dashboard.repository.TaskStatisticsRepository;
import com.example.outsourcingproject.domain.dashboard.dto.TodayTaskItemDto;
import com.example.outsourcingproject.domain.task.enums.TaskStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final TaskStatisticsRepository taskStatisticsRepository;
    private final AcitivityFeedRepository acitivityFeedRepository;

    /**
     * 기준일(date)을 포함한 주(월~일)와 그 전 주를 비교해 통계와 주간 증감률을 반환.
     * 날짜를 적지 않으면 deafult = 오늘
     */
    public DashboardStatisticsDto getWeeklyStatistics(LocalDate date) {


        LocalDate baseDate = (date != null) ? date : LocalDate.now();
        LocalDate monday = baseDate.with(DayOfWeek.MONDAY);
        LocalDateTime weekStart = monday.atStartOfDay();
        LocalDateTime weekEnd = monday.plusWeeks(1).atStartOfDay();


        LocalDateTime prevWeekStart = monday.minusWeeks(1).atStartOfDay();
        LocalDateTime prevWeekEnd = weekStart;

        long total = taskStatisticsRepository.countByCreatedAtBetween(weekStart, weekEnd);
        long todo = taskStatisticsRepository.countByStatusAndPeriod(TaskStatus.TODO, weekStart, weekEnd);
        long inProgress = taskStatisticsRepository.countByStatusAndPeriod(TaskStatus.IN_PROGRESS, weekStart, weekEnd);
        long done = taskStatisticsRepository.countByStatusAndPeriod(TaskStatus.DONE, weekStart, weekEnd);
        long overdue = taskStatisticsRepository.countOverdueTasksInPeriod(
                List.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS),
                LocalDateTime.now(),
                weekStart,
                weekEnd
        );

        double completionRate = rate(done, total);
        double inProgressRate = rate(inProgress, total);
        double overdueRate = rate(overdue, total);


        //주간 증가율 계산
        long lastWeekTotal = taskStatisticsRepository.countByCreatedAtBetween(prevWeekStart, prevWeekEnd);

        double weeklyChangeRate = (lastWeekTotal == 0)
                ? (total > 0 ? 100.0 : 0.0)
                : Math.round((total - lastWeekTotal) * 10000.0 / lastWeekTotal) / 100.0;

        return DashboardStatisticsDto.of(
                total,
                weeklyChangeRate,
                done,
                completionRate,
                inProgress,
                inProgressRate,
                todo,
                overdue,
                overdueRate
        );
    }


    // 비율 계산 유틸
    private double rate(long part, long total) {
        return (total == 0) ? 0.0 : Math.round(part * 10000.0 / total) / 100.0;
    }


    /**
     * 오늘 마감 예정이면서 아직 기한이 남은 작업(TO-DO, IN_PROGRESS) 중
     * 마감 임박 순(deadline ASC) 상위 5개를 조회한다.
     * <p>
     * now  : 현재 시각 (over-due 커트라인)
     * end  : 내일 00:00 (= 오늘 23:59:59 포함)
     */
    public List<TodayTaskItemDto> getMyTodayTasks(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay(); // 내일 00:00 = 오늘의 끝

        Pageable topFive = PageRequest.of(0, 5);

        return taskStatisticsRepository.findTodayTasks(
                userId,
                List.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS),
                now, end,
                topFive
        );

    }

//
//    public List<TodayTaskItemDto> getMyTasksByDate(Long userId, LocalDate date) {
//        List<Task> tasks = taskStatisticsRepository.findAllByUserIdAndDate(
//                userId,
//                date,
//                List.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS));
//        return tasks.stream()
//                .map(TodayTaskItemDto::from)
//                .collect(Collectors.toList());
//
//    }


    /**
     * 입력한 날짜가 속한 주의 월~일 작업 추이
     * 날짜를 적지 않으면 deafult = 오늘
     */
    public List<DailyTaskTrendDto> getWeeklyTrend(LocalDate date) {
        LocalDate baseDate = (date != null) ? date : LocalDate.now();
        // 기준 날짜가 속한 주의 월요일 00:00부터
        LocalDate monday = baseDate.with(java.time.DayOfWeek.MONDAY);
        LocalDateTime start = monday.atStartOfDay();
        // 다음 주 월요일 00:00(미포함)까지 범위 조회
        LocalDateTime end = monday.plusWeeks(1).atStartOfDay();
        // DB 조회
        List<DailyTaskTrendProjection> raw = taskStatisticsRepository.fetchDailyTrend(start, end);

        // 1. 결과를 Map으로 변환
        Map<LocalDate, DailyTaskTrendProjection> map = raw.stream()
                .collect(Collectors.toMap(DailyTaskTrendProjection::getDate, p -> p));

        // 2. 월~일 모두 돌면서 결과 만들기
        List<DailyTaskTrendDto> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            DailyTaskTrendProjection p = map.get(day);

            result.add(DailyTaskTrendDto.of(
                    day,
                    (p != null) ? p.getTotalCount() : 0,
                    (p != null) ? p.getDoneCount() : 0
            ));
        }

        return result;

    }


    /**
     * 전체 작업 상태 비율 반환 (TODO, IN_PROGRESS, DONE)
     */
    public TaskStatusRatioDto getStatusRatio() {

        List<TaskStatusCountDto> result = taskStatisticsRepository.countGroupByStatus();

        long todo = 0, inProgress = 0, done = 0;

        for (TaskStatusCountDto dto : result) {
            switch (dto.getStatus()) {
                case TODO -> todo = dto.getCount();
                case IN_PROGRESS -> inProgress = dto.getCount();
                case DONE -> done = dto.getCount();
            }
        }

        return TaskStatusRatioDto.of(todo, inProgress, done);

    }

    /**
     * 내 작업과 팀 전체 작업의 완료율 비교
     */
    public ProgressRatioDto getProgressRatio(Long userId) {

        //내 상태별 카운트
        Map<TaskStatus, Long> myMap = toMap(taskStatisticsRepository.countMyStatus(userId));

        //팀 상태별 카운트
        Map<TaskStatus, Long> teamMap = toMap(taskStatisticsRepository.countTeamStatus());


        long myDone = myMap.getOrDefault(TaskStatus.DONE, 0L);
        long myTotal = myMap.values().stream().mapToLong(Long::longValue).sum();

        long teamDone = teamMap.getOrDefault(TaskStatus.DONE, 0L);
        long teamTotal = teamMap.values().stream().mapToLong(Long::longValue).sum();

        double myRate = rate(myDone, myTotal);
        double teamRate = rate(teamDone, teamTotal);

        return ProgressRatioDto.of(myRate, teamRate);

    }


    // 리스트를 상태별 Map으로 변환
    private Map<TaskStatus, Long> toMap(List<TaskStatusCountDto> list) {

        return list.stream()
                .collect(Collectors.toMap(
                        TaskStatusCountDto::getStatus,
                        TaskStatusCountDto::getCount));

    }


    /**
     * 올해 1~12월 월별 작업/완료 수 트렌드 반환
     */
    public List<MonthlyTaskTrendDto> getMonthlyTrend() {
        int year = Year.now().getValue();

        // projection → DTO 변환
        List<MonthlyTaskTrendDto> result = taskStatisticsRepository.fetchFixedMonthlyTrend(year).stream()
                .map(p -> MonthlyTaskTrendDto.of(p.getMonth(), p.getTotalCount(), p.getDoneCount()))
                .collect(Collectors.toList());

        Map<Integer, MonthlyTaskTrendDto> map = result.stream()
                .collect(Collectors.toMap(
                        MonthlyTaskTrendDto::getMonth,
                        dto -> dto
                ));

        List<MonthlyTaskTrendDto> fullYear = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            fullYear.add(map.getOrDefault(m, MonthlyTaskTrendDto.of(m, 0, 0)));
        }

        return fullYear;

    }


    /**
     * 최근 일주일간 활동 피드 최신순 5개 반환
     */
    public List<ActivityFeedDto> getAcitivityFeed() {
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(7);

        Pageable topFive = PageRequest.of(0, 5, // 0페이지, 5건
                Sort.by(Sort.Direction.DESC, "createdAt")); // 최신순

        return acitivityFeedRepository.fetchFeed(from, to, topFive);

    }


}
