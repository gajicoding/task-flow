package com.example.outsourcingproject.global.aop.event;

import com.example.outsourcingproject.global.aop.event.dto.ActivityLogEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityLogPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public void publish(ActivityLogEventDto eventDto) {
        eventPublisher.publishEvent(eventDto);
    }
}
