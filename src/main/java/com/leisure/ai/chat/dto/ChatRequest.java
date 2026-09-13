package com.leisure.ai.chat.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ChatRequest(
        @NotBlank String question,
        List<Turn> history // 최근 3턴, 메인 백엔드가 이미 잘라서 보냄. 없으면 QueryRewriter가 리라이팅 스킵
) {

    public record Turn(String role, String content) {
    }
}
