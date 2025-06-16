package com.example.outsourcingproject.domain.auth.entity;

import com.example.outsourcingproject.domain.auth.enums.UserRole;
import com.example.outsourcingproject.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auth")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auth extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(unique = true, nullable = false, length = 20)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    public Auth(String name, String username, String email, String password, UserRole role) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
