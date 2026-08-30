package com.leisure.ai.global.exception;

// TODO
// - @RestControllerAdvice로 공통 예외 처리
// - Qdrant 호출 실패, ONNX 추론 실패, Ollama 타임아웃 등을 ApiResponse 실패 포맷으로 매핑
// - 검증 실패(@Valid) -> 400, 내부 인증 실패는 InternalAuthFilter에서 이미 처리하므로 여기선 제외
