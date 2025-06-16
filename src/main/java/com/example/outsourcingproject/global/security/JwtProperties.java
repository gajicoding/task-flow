package com.example.outsourcingproject.global.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secretKey;

    private Long expirationTime = 900_000L;

    private Long refreshExpirationTime = 1_209_600_000L;

    private String issuer = "taskflow-app";

    private String bearerPrefix = "Bearer ";
}