package com.example.outsourcingproject.domain.task.enums;

import com.example.outsourcingproject.domain.task.exception.TaskException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Priority {
    LOW("낮음"),
    MEDIUM("중간"),
    HIGH("높음");

    private final String description;

    Priority(String description) {
        this.description = description;
    }

    // 응답 -> 항상 영어 값으로 반환
    @JsonValue
    public String getDescription() {
        return name();
    }

    // 한글 또는 영어 값으로 입력 가능하도록 함
    @JsonCreator
    public static Priority fromString(String value) {
        for (Priority priority : Priority.values()) {
            if (priority.name().equalsIgnoreCase(value) || priority.description.equalsIgnoreCase(value)) {
                return priority;
            }
        }
        throw new TaskException(TaskErrorCode.INVALID_TASK_PRIORITY);
    }

}
