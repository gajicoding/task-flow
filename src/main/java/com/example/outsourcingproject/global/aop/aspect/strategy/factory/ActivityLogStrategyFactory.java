package com.example.outsourcingproject.global.aop.aspect.strategy.factory;

import com.example.outsourcingproject.global.aop.aspect.strategy.ActivityLogStrategy;
import com.example.outsourcingproject.global.enums.TargetType;
import org.aspectj.lang.ProceedingJoinPoint;
import com.example.outsourcingproject.global.aop.annotation.ActivityLog;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ActivityLogStrategyFactory {

    private final Map<TargetType, ActivityLogStrategy> strategyMap = new EnumMap<>(TargetType.class);

    public ActivityLogStrategyFactory(List<ActivityLogStrategy> strategies) {
        for (ActivityLogStrategy strategy: strategies) {
            strategyMap.put(strategy.getTargetType(), strategy);
        }
    }

    public ActivityLogStrategy create(TargetType targetType, ProceedingJoinPoint joinPoint, ActivityLog activityLog, Object response) {

        ActivityLogStrategy strategy = strategyMap.get(targetType);
        if (strategy == null) {
            throw new IllegalArgumentException("지원하지 않는 TargetType: " + targetType);
        }

        strategy.initialize(joinPoint, activityLog, response);

        return strategy;
    }
}
