package com.example.outsourcingproject.domain.comment.service;

import com.example.outsourcingproject.domain.comment.dto.CommentRequestDto;
import com.example.outsourcingproject.domain.comment.dto.CommentResponseDto;
import com.example.outsourcingproject.domain.comment.entity.Comment;
import com.example.outsourcingproject.domain.comment.exception.CustomException;
import com.example.outsourcingproject.domain.comment.exception.error.CustomErrorCode;
import com.example.outsourcingproject.domain.comment.repository.CommentRepository;
import com.example.outsourcingproject.domain.task.entity.Task;
import com.example.outsourcingproject.domain.task.repository.TaskRepository;
import com.example.outsourcingproject.domain.user.entity.User;
import com.example.outsourcingproject.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public CommentResponseDto createComment(Long taskId, Long userId, CommentRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.USER_NOT_FOUND));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.TASK_NOT_FOUND));

        Comment comment = Comment.builder()
                .user(user)
                .task(task)
                .content(requestDto.getContent())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        Comment saved = commentRepository.save(comment);
        return new CommentResponseDto(saved);
    }

    @Transactional
    public CommentResponseDto updateComment(Long taskId, Long commentId, CommentRequestDto requestDto) {
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.COMMENT_NOT_FOUND));

        comment.updateContent(requestDto.getContent());
        return new CommentResponseDto(comment);
    }

    @Transactional
    public void deleteComment(Long taskId, Long commentId) {
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.COMMENT_NOT_FOUND));
        comment.softDelete();
    }

    public List<CommentResponseDto> getCommentsByTask(Long taskId) {
        return commentRepository.findByTaskIdAndIsDeletedFalseOrderByCreatedAtDesc(taskId).stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<CommentResponseDto> searchComments(Long taskId, String keyword) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.TASK_NOT_FOUND));

        return commentRepository.findAllByTaskAndContentContainingIgnoreCaseOrderByCreatedAtDesc(task, keyword).stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }
}
