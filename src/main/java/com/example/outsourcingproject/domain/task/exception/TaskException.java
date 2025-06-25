package com.example.outsourcingproject.domain.task.exception;

import com.example.outsourcingproject.domain.task.enums.TaskErrorCode;
import lombok.Getter;

@Getter
public class TaskException extends RuntimeException {

    private final TaskErrorCode errorCode;

    public TaskException(TaskErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
