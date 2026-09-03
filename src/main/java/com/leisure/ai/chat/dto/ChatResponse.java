package com.leisure.ai.chat.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

// SSE로 흘려보낼 이벤트. "type" 필드로 프론트가 토큰인지 종료 신호인지 구분.
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ChatResponse.Token.class, name = "token"),
        @JsonSubTypes.Type(value = ChatResponse.Done.class, name = "done")
})
public sealed interface ChatResponse permits ChatResponse.Token, ChatResponse.Done {

    record Token(String content) implements ChatResponse {
    }

    // 스트림 종료 시 1회 전송, 프롬프트 컨텍스트에 실제로 쓰인 게시글/축제 id 목록
    record Done(List<Long> postIds) implements ChatResponse {
    }
}
