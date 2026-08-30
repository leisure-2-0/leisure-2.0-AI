package com.leisure.ai.chat.pipeline;

// TODO (로직 문서 11단계)
// - PromptTemplates의 시스템 프롬프트("아래 글에만 근거해 답변. 없으면 모른다고 답할 것") 사용
// - 컨텍스트: 정렬된 문서들의 title + content 조립
// - 질문: rewrittenQuery
// - Spring AI ChatClient.prompt().system(...).user(...) 형태로 넘길 최종 프롬프트 구성
