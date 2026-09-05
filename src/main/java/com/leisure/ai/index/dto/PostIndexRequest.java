package com.leisure.ai.index.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// 게시글 컬렉션
public record PostIndexRequest(
        @NotNull Long postId,
        @NotBlank String title,
        @NotBlank String content,
        String region,
        String regionDetail,
        String category,
        List<String> tags,
        Long viewCount,
        Long likeCount,
        @NotNull Long createdAt // epoch seconds
) {
}
