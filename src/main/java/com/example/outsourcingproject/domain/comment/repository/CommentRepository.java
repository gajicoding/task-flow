package com.example.outsourcingproject.domain.comment.repository;

import com.example.outsourcingproject.domain.auth.entity.Auth;
import com.example.outsourcingproject.domain.comment.entity.Comment;
import com.example.outsourcingproject.domain.task.entity.Task;
import com.example.outsourcingproject.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findByIdAndIsDeletedFalse(Long id);

    List<Comment> findAllByTaskOrderByCreatedAtDesc(Task task);

    List<Comment> findAllByTaskAndIsDeletedFalseAndContentContainingIgnoreCaseOrderByCreatedAtDesc(Task task, String keyword);

    List<Comment> findAllByTaskAndContentContainingIgnoreCaseOrderByCreatedAtDesc(Task task, String keyword);

    List<Comment> findAllByAuth(Auth auth);

    List<Comment> findByTaskIdAndIsDeletedFalseOrderByCreatedAtDesc(Long taskId);

    List<Comment> findByContentContainingAndIsDeletedFalseOrderByCreatedAtDesc(String keyword);

    // 페이징 처리된 Task 댓글 조회
    Page<Comment> findByTaskAndIsDeletedFalseOrderByCreatedAtDesc(Task task, Pageable pageable);


}
