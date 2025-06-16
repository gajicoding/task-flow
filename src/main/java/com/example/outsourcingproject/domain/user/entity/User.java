package com.example.outsourcingproject.domain.user.entity;

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
    private Long id; // Auth와 동일한 PK

    @Column(nullable = false, length = 50)
    private String name; // 캐시용

    @Column(unique = true, nullable = false, length = 100)
    private String email; // 캐시용

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}