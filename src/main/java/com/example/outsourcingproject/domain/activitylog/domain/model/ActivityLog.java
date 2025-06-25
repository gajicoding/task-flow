package com.example.outsourcingproject.domain.activitylog.domain.model;

import com.example.outsourcingproject.domain.auth.entity.Auth;
import com.example.outsourcingproject.global.enums.ActivityType;
import com.example.outsourcingproject.global.enums.RequestMethod;
import com.example.outsourcingproject.global.enums.TargetType;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Getter
@EntityListeners(AuditingEntityListener.class)
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Auth auth;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;

    private Long targetId;

    private String message;

    private String requestIp;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_method", nullable = false)
    private RequestMethod requestMethod;

    private String requestUrl;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    protected ActivityLog() {}

    public ActivityLog(Auth auth, ActivityType activityType, TargetType targetType, Long targetId, String message, String requestIp, RequestMethod requestMethod, String requestUrl) {
        this.auth = auth;
        this.activityType = activityType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.message = message;
        this.requestIp = requestIp;
        this.requestMethod = requestMethod;
        this.requestUrl = requestUrl;
    }
}
