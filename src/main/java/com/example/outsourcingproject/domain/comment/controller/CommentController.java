package com.example.outsourcingproject.domain.comment.controller;

import com.example.outsourcingproject.domain.comment.dto.CommentRequestDto;
import com.example.outsourcingproject.domain.comment.dto.CommentResponseDto;
import com.example.outsourcingproject.domain.comment.service.CommentService;
import com.example.outsourcingproject.global.dto.ApiResponse;
import com.example.outsourcingproject.global.dto.PagedResponse;
import com.example.outsourcingproject.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<ApiResponse<CommentResponseDto>> createComment(
            @PathVariable Long taskId,
            @RequestBody CommentRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long userId = userPrincipal.getId();
        CommentResponseDto responseDto = commentService.createComment(taskId, userId, requestDto);
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    // 댓글 수정
    @PutMapping("/tasks/{taskId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponseDto>> updateComment(
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto requestDto) {

        CommentResponseDto responseDto = commentService.updateComment(taskId, commentId, requestDto);
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    // 댓글 삭제 (Soft Delete)
    @DeleteMapping("/tasks/{taskId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        commentService.deleteComment(taskId, commentId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 댓글 페이징 조회
    @GetMapping("/tasks/{taskId}/comments")
    public ResponseEntity<ApiResponse<PagedResponse<CommentResponseDto>>> getCommentsByTaskPaged(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        int correctedPage = page < 1 ? 0 : page - 1; // 1-based -> 0-based 변환

        Pageable pageable = PageRequest.of(correctedPage, size, Sort.by("createdAt").descending());
        Page<CommentResponseDto> pageResult = commentService.getCommentsByTaskPaged(taskId, pageable);

        PagedResponse<CommentResponseDto> pagedResponse = PagedResponse.toPagedResponse(pageResult);
        ApiResponse<PagedResponse<CommentResponseDto>> response = ApiResponse.success(pagedResponse);

        return ResponseEntity.ok(response);
    }
}
