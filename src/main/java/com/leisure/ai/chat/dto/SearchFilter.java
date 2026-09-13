package com.leisure.ai.chat.dto;

public record SearchFilter(
        String region,
        Long endDateAfter // 축제 검색일 때만 CollectionRouter가 채움 (end_date >= 오늘)
) {
}
