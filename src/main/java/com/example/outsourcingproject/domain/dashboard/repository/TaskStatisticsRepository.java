package com.example.outsourcingproject.domain.dashboard.repository;


import com.example.outsourcingproject.domain.dashboard.dto.TaskStatusCountDto;
import com.example.outsourcingproject.domain.dashboard.dto.TodayTaskItemDto;
import com.example.outsourcingproject.domain.dashboard.projection.DailyTaskTrendProjection;
import com.example.outsourcingproject.domain.dashboard.projection.MonthlyTaskTrendProjection;
import com.example.outsourcingproject.domain.task.entity.Task;
import com.example.outsourcingproject.domain.task.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;


import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface TaskStatisticsRepository extends JpaRepository<Task, Long> {

    // ==========================  통계용 조회 ==========================

    /**
     * 기간 내 전체 태스크 개수 조회 (createdAt 기준)
     */
    @Query("""
            SELECT COUNT(t)
            FROM Task t
            WHERE t.isDeleted = false
            AND t.createdAt BETWEEN :from AND :to
            
            """
    )
    long countByCreatedAtBetween(@Param("from") LocalDateTime from,
                                 @Param("to") LocalDateTime to);


    /**
     * 기간 + 상태 조건에 따른 태스크 개수 조회
     */
    @Query("""
            SELECT COUNT(t)
            FROM Task t
            WHERE t.status IN :status
            AND t.isDeleted = false 
            AND t.createdAt BETWEEN :from AND :to
            """)
    long countByStatusAndPeriod(@Param("status") TaskStatus status,
                                @Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to);



    /**
     * 기간 내 마감 기한이 지나간 태스크 개수 조회 (overdue)
     */
    @Query("""
                SELECT COUNT(t)
                FROM Task t
                WHERE t.status IN (:statuses)
                  AND t.deadline < :now
                  AND t.deadline BETWEEN :from AND :to
                  AND t.isDeleted = false
            """)
    long countOverdueTasksInPeriod(@Param("statuses") List<TaskStatus> statuses,
                                   @Param("now") LocalDateTime now,
                                   @Param("from") LocalDateTime from, //weekStart
                                   @Param("to") LocalDateTime to); //weekEnd


    // ========================== 오늘 작업 ==========================

    /**
     * 오늘 마감 예정이고 기한이 남은 할 일 5개 조회 (TO-DO, IN_PROGRESS)
     */
    @Query("""
            SELECT new com.example.outsourcingproject.domain.dashboard.dto.TodayTaskItemDto(
                    t.id, t.title, t.status, t.priority, t.deadline)
            FROM Task t
            WHERE t.assignee.id = :userId
             AND t.isDeleted = false
             AND t.status IN (:statuses)
             AND t.deadline >= :now          
             AND t.deadline <  :end           
            ORDER BY t.deadline ASC 
            """)
    List<TodayTaskItemDto> findTodayTasks(
            @Param("userId") Long userId,
            @Param("statuses") List<TaskStatus> statuses,
            @Param("now") LocalDateTime now, // overdue 제외
            @Param("end") LocalDateTime end,
            Pageable pageable
    );


// /**
// //     * 날짜 기준으로 특정 유저의 할 일 조회 (사용 안함)
// //     */
//    @Query("""
//            SELECT t
//            FROM Task t
//            WHERE t.assigneeId = :userId
//            AND DATE(t.deadline) = :date
//            AND t.status IN (:statuses)
//            AND t.isDeleted = false
//                        """)
//    List<Task> findAllByUserIdAndDate(@Param("userId") Long userId,
//                                      @Param("date") LocalDate date,
//                                      @Param("statuses") List<TaskStatus> statuses);




    // ==========================  주간 트렌드 ==========================

    /**
     * 최근 7일간 일자별 태스크 생성/완료 수 조회 (native, created_at 기준)
     */
    @Query(value = """
                SELECT DATE(t.created_at) AS date,
                       COUNT(*) AS totalCount,
                       SUM(CASE WHEN t.status = 'DONE' THEN 1 ELSE 0 END) AS doneCount
                FROM tasks t
                WHERE t.created_at BETWEEN :start AND :end
                  AND t.is_deleted = false
                GROUP BY DATE(t.created_at)
                ORDER BY DATE(t.created_at)
            """, nativeQuery = true)
    List<DailyTaskTrendProjection> fetchDailyTrend(@Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);

    // ========================== 상태 비율 ==========================

    /**
     * 전체 태스크 상태별 개수 통계 (전체 비율용)
     */
    @Query("""
                SELECT new com.example.outsourcingproject.domain.dashboard.dto.TaskStatusCountDto(
                    t.status,
                    COUNT(t)
                )
                FROM Task t
                WHERE t.isDeleted = false
                GROUP BY t.status
            """)
    List<TaskStatusCountDto> countGroupByStatus();



    /**
     * 특정 유저의 상태별 태스크 개수 (개인 진행률용)
     */
    @Query("""
                     SELECT new com.example.outsourcingproject.domain.dashboard.dto.TaskStatusCountDto(
                                t.status, COUNT(t))
                     FROM Task t
                     WHERE t.assignee.id = :userId
                     AND t.isDeleted = false
                     GROUP BY t.status
            """)
    List<TaskStatusCountDto> countMyStatus(@Param("userId") Long userId);


    /**
     * 전체 팀의 상태별 태스크 개수 (팀 진행률용)
     */
    @Query("""
                SELECT new com.example.outsourcingproject.domain.dashboard.dto.TaskStatusCountDto(
                       t.status,
                       COUNT(t))
                  FROM Task t
                 WHERE t.isDeleted = false
                 GROUP BY t.status
            """)
    List<TaskStatusCountDto> countTeamStatus();



    // ========================== 월간 트렌드 ==========================

    /**
     * 월별 태스크 생성/완료 수 조회 (특정 연도 기준, native)
     */
    @Query(value = """
                SELECT MONTH(t.created_at) AS month,
                       COUNT(*) AS totalCount,
                       SUM(CASE WHEN t.status = 'DONE' THEN 1 ELSE 0 END) AS doneCount
                FROM tasks t
                WHERE YEAR(t.created_at) = :year
                  AND t.is_deleted = false
                GROUP BY MONTH(t.created_at)
                ORDER BY MONTH(t.created_at)
            """, nativeQuery = true)
    List<MonthlyTaskTrendProjection> fetchFixedMonthlyTrend(@Param("year") int year);


}
