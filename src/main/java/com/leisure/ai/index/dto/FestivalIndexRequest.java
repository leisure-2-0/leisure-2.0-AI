package com.leisure.ai.index.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 축제 컬렉션
public record FestivalIndexRequest(
        @NotNull Long festivalId,
        @NotBlank String title,
        String description,
        String address,
        String region,
        Long startDate, 
        @NotNull Long endDate 
) {
}
