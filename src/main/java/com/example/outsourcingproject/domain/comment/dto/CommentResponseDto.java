package com.example.outsourcingproject.domain.comment.dto;

import com.example.outsourcingproject.domain.auth.entity.Auth;
import com.example.outsourcingproject.domain.comment.entity.Comment;
import com.example.outsourcingproject.domain.user.dto.UserResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
public class CommentResponseDto {
    private Long id;
    private Long userId;
    private String content;
    private Long taskId;

    @Setter
    private UserResponseDto user;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Comment 엔티티에서 DTO 변환 생성자
    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.userId = comment.getAuth().getId();
        this.content = comment.getContent();
        this.taskId = comment.getTask().getId();
        this.user = new UserResponseDto(comment.getAuth());
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
    }

}
