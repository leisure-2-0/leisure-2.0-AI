package com.leisure.ai.index.dto;

import jakarta.validation.constraints.NotNull;

// 배치 업데이트 
public record StatsUpdateRequest(
        @NotNull Long postId,
        @NotNull Long viewCount,
        @NotNull Long likeCount 
) {
}
