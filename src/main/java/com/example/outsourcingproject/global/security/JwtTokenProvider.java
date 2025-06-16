package com.example.outsourcingproject.global.security;

import com.example.outsourcingproject.domain.auth.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(jwtProperties.getSecretKey());
        key = Keys.hmacShaKeyFor(bytes);
    }

    public String createToken(Long userId, String email, UserRole userRole) {
        Date date = new Date();
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .id(jti)
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("userRole", userRole.name())
                .issuer(jwtProperties.getIssuer())
                .expiration(new Date(date.getTime() + jwtProperties.getExpirationTime()))
                .issuedAt(date)
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!jwtProperties.getIssuer().equals(claims.getIssuer())) {
                throw new BadCredentialsException("유효하지 않은 토큰입니다.");
            }

            return claims;

        } catch (ExpiredJwtException e) {
            throw new CredentialsExpiredException("토큰이 만료되었습니다.");

        } catch (Exception e) {
            throw new BadCredentialsException("유효하지 않은 토큰입니다.");
        }
    }

    public LocalDateTime getExpirationTime(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}