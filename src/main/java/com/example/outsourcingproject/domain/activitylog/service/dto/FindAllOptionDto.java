package com.example.outsourcingproject.domain.activitylog.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class FindAllOptionDto {
    private Pageable pageable;

    private Long userId;
    private String activityType;
    private Long taskId;

    private LocalDate startDate;
    private LocalDate endDate;
}
