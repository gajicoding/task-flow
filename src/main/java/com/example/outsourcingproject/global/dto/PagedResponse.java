package com.example.outsourcingproject.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class PagedResponse<T> {
    private final List<T> content;
    private final long totalElements;
    private final int totalPages;
    private final int size;
    private final long number;

    public static <T> PagedResponse<T> toPagedResponse(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber() + 1L // 0-based index → 1-based index
        );
    }
}
