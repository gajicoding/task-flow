package com.example.outsourcingproject.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class ProgressRatioDto {
    private  double myRate;
    private double teamRate;
}
