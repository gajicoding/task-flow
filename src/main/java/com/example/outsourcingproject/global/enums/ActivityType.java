package com.example.outsourcingproject.global.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum ActivityType {
    TASK_CREATED("작업 생성", "새로운 작업이 생성되었습니다.", "\"%s\" 작업을 생성했습니다."),
    TASK_UPDATED("작업 수정", "작업이 수정되었습니다.", "\"%s\" 작업을 수정했습니다."),
    TASK_DELETED("작업 삭제", "작업이 삭제되었습니다.", "\"%s\" 작업을 삭제했습니다."),
    TASK_STATUS_CHANGED("작업 상태 변경", "작업 상태가 %s 상태로 변경되었습니다.", "\"%s\" 작업을 %s로 이동했습니다."),
    COMMENT_CREATED("댓글 작성", "댓글이 작성되었습니다.", "댓글이 작성되었습니다."),
    COMMENT_UPDATED("댓글 수정", "댓글이 수정되었습니다.", "댓글이 수정되었습니다."),
    COMMENT_DELETED("댓글 삭제", "댓글이 삭제되었습니다.", "댓글이 삭제되었습니다."),
    USER_LOGGED_IN("사용자 로그인", "사용자가 로그인했습니다.", "\"%s\"님이 로그인 했습니다."),
    USER_LOGGED_OUT("사용자 로그아웃", "사용자가 로그아웃했습니다.", "\"%s\"님이 로그아웃했습니다.");

    private final String label;
    private final String message1;
    private final String message2;

    ActivityType(String label, String message1, String message2) {
        this.label = label;
        this.message1 = message1;
        this.message2 = message2;
    }

    public static Optional<ActivityType> of(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(input))
                .findFirst();
    }
}
