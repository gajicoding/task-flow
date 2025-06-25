package com.example.outsourcingproject.domain.comment.service;

import com.example.outsourcingproject.domain.auth.entity.Auth;
import com.example.outsourcingproject.domain.auth.exception.UserNotFoundException;
import com.example.outsourcingproject.domain.auth.repository.AuthRepository;
import com.example.outsourcingproject.domain.comment.dto.CommentRequestDto;
import com.example.outsourcingproject.domain.comment.dto.CommentResponseDto;
import com.example.outsourcingproject.domain.comment.entity.Comment;
import com.example.outsourcingproject.domain.comment.exception.CustomException;
import com.example.outsourcingproject.domain.comment.exception.error.CustomErrorCode;
import com.example.outsourcingproject.domain.comment.repository.CommentRepository;
import com.example.outsourcingproject.domain.task.entity.Task;
import com.example.outsourcingproject.domain.task.repository.TaskRepository;
import com.example.outsourcingproject.domain.user.dto.UserResponseDto;
import com.example.outsourcingproject.domain.user.entity.User;
import com.example.outsourcingproject.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public CommentResponseDto createComment(Long taskId, Long userId, CommentRequestDto requestDto) {
        Auth auth = authRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.USER_NOT_FOUND));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.TASK_NOT_FOUND));

        Comment comment = Comment.builder()
                .auth(auth)
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
    public void deleteComment(Long taskId, Long commentId, Long userId) {
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.COMMENT_NOT_FOUND));

        // 🔐 본인 확인
        if (!comment.getAuth().getId().equals(userId)) {
            throw new CustomException(CustomErrorCode.UNAUTHORIZED);
        }

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

    public Page<CommentResponseDto> getCommentsByTaskPaged(Long taskId, Pageable pageable) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.TASK_NOT_FOUND));

        Page<Comment> commentPage = commentRepository.findByTaskAndIsDeletedFalseOrderByCreatedAtDesc(task, pageable);

        return commentPage.map(CommentResponseDto::new);
    }
}
