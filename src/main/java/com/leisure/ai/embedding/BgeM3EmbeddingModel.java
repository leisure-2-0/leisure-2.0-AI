package com.leisure.ai.embedding;

// TODO
// - org.springframework.ai.embedding.EmbeddingModel 인터페이스를 구현 (새 인터페이스 만들지 말 것 -
//   Spring AI의 VectorStore가 이 표준 인터페이스 타입으로 EmbeddingModel을 주입받음)
// - @Component로 등록하면 Spring AI가 자동 인식 -
//   단, Ollama starter가 자체 EmbeddingModel 빈을 자동 등록하면 충돌 가능 ->
//   application.yml에서 spring.ai.ollama.embedding.enabled=false 확인
// - 내부: OnnxConfig의 공유 OrtEnvironment + bge-m3 tokenizer(ai.djl.huggingface.tokenizers)로
//   텍스트 -> 토큰 -> ONNX 세션 실행 -> float[1024] 반환
// - TextPreprocessor가 조립한 텍스트를 입력으로 받음 (제목+본문+태그명, 또는 축제 자연문)
