package com.example.outsourcingproject.domain.task.enums;

import com.example.outsourcingproject.domain.task.exception.TaskException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TaskStatus {
    TODO("할 일"),
    IN_PROGRESS("진행 중"),
    DONE("완료");

    private final String description;

    TaskStatus(String description) {
        this.description = description;
    }

    @JsonValue
    public String getDescription() {
        return name();
    }

    // 한글 또는 영어 값으로 입력 가능하도록 함
    @JsonCreator
    public static TaskStatus fromString(String value) {
        for (TaskStatus taskStatus : TaskStatus.values()) {
            if (taskStatus.name().equalsIgnoreCase(value) || taskStatus.description.equalsIgnoreCase(value)) {
                return taskStatus;
            }
        }
        throw new TaskException(TaskErrorCode.INVALID_TASK_STATUS);
    }

}
