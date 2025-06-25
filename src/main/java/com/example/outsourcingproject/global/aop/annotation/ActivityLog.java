package com.example.outsourcingproject.global.aop.annotation;

import com.example.outsourcingproject.global.enums.ActivityType;
import com.example.outsourcingproject.global.enums.TargetType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivityLog {
    ActivityType type();
    TargetType target();
}