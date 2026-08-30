package com.leisure.ai.embedding;

// TODO
// - RerankerModel 구현체
// - OnnxConfig의 공유 OrtEnvironment + bge-reranker-v2-m3 tokenizer로
//   (query, document) 쌍마다 ONNX 세션 실행 -> 관련성 점수 반환
// - 질문 1개 + 문서 10개 -> 10회 추론 (배치 추론으로 묶을 수 있는지 확인 - 성능 이슈 생기면 고려)
