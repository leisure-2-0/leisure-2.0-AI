package com.leisure.ai.chat.dto;

public record QueryIntent(
        String rewrittenQuery,
        SortMode sortMode,
        boolean isFestivalQuery
) {

    public enum SortMode { RELEVANCE, POPULARITY, RECENT }

    // QueryRewriter의 LLM 호출/구조화 출력 파싱이 실패했을 때 사용
    public static QueryIntent fallback(String question) {
        return new QueryIntent(question, SortMode.RELEVANCE, false);
    }
}
