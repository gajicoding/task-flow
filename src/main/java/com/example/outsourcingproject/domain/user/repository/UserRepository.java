package com.example.outsourcingproject.domain.user.repository;

import com.example.outsourcingproject.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdAndIsDeletedFalse(Long id);
}
