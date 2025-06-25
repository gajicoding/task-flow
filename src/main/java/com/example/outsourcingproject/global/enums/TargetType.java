package com.example.outsourcingproject.global.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum TargetType {
    TASK(1),
    COMMENT(2),
    USER(3);

    private final int id;

    TargetType(int id) {
        this.id = id;
    }

    public static Optional<TargetType> fromId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }

        return Arrays.stream(values())
                .filter(e -> id.equals(e.id))
                .findFirst();
    }
}
