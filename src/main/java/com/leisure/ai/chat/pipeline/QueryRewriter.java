package com.leisure.ai.chat.pipeline;

// TODO (로직 문서 5단계)
// - Spring AI ChatClient로 LLM 1회 호출해 리라이팅 + 의도 추출
// - history가 없으면 LLM 호출 스킵하고 원문 question 그대로 사용
// - 출력 파싱: { rewrittenQuery, sortMode, isFestivalQuery }
// - JSON 파싱 실패 시 QueryIntent 폴백값 반환 (question 원문, RELEVANCE, false)
