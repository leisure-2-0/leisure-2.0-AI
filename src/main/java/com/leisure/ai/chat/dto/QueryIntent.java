package com.leisure.ai.chat.dto;

public record QueryIntent(
        String rewrittenQuery,
        SortMode sortMode,
        boolean isFestivalQuery
) {

    public enum SortMode { RELEVANCE, POPULARITY, RECENT }

    // QueryRewriter의 LLM 출력 파싱 실패 시, 혹은 history가 없어 리라이팅을 스킵할 때 사용
    public static QueryIntent fallback(String question) {
        return new QueryIntent(question, SortMode.RELEVANCE, false);
    }
}
