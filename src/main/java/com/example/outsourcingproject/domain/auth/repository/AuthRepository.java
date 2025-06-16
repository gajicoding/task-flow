package com.example.outsourcingproject.domain.auth.repository;

import com.example.outsourcingproject.domain.auth.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<Auth, Long> {

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @Query("SELECT a FROM Auth a WHERE a.username = :username AND a.isDeleted = false")
    Optional<Auth> findActiveByUsername(@Param("username") String username);

    Optional<Auth> findByIdAndIsDeletedFalse(Long id);
}