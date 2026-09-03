package com.leisure.ai.chat.pipeline;

public final class PromptTemplates {
    public static final String SYSTEM = """
            당신은 여행/여가 정보를 안내하는 어시스턴트입니다.
            아래 [참고 글]에만 근거해서 답변하세요. 참고 글에 없는 내용은 답변에 포함하지 말고,
            참고 글만으로 답을 알 수 없으면 모른다고 답하세요.
            """;

    private PromptTemplates() {
    }
}
