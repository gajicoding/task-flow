package com.example.outsourcingproject.domain.user.entity;

import com.example.outsourcingproject.domain.user.enums.UserRole;
import com.example.outsourcingproject.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String username;  // 로그인 아이디

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;      // 실명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role; // USER, ADMIN

    public User(String name, String username, String email, String password, UserRole userRole) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = userRole;
    }
}