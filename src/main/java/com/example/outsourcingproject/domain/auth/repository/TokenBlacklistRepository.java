package com.example.outsourcingproject.domain.auth.repository;

import com.example.outsourcingproject.domain.auth.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    boolean existsByJtiAndExpiryTimeAfter(String jti, LocalDateTime now);

    boolean existsByJti(String jti);

    @Modifying
    @Query("DELETE FROM TokenBlacklist t WHERE t.expiryTime < :expiredTime")
    void deleteExpiredTokens(@Param("expiredTime") LocalDateTime expiredTime);
}