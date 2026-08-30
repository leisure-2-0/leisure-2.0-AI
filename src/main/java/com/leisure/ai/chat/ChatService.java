package com.leisure.ai.chat;

// TODO - 파이프라인 오케스트레이션 (로직 문서 B. 채팅 처리 5~13단계)
// 1. QueryRewriter로 리라이팅 + 의도 추출 (history 없으면 스킵)
// 2. CollectionRouter로 posts/festivals 라우팅 결정
// 3. Retriever로 임베딩 + Qdrant 검색 (Spring AI VectorStore.similaritySearch, top 10)
// 4. Reranker로 상위 5개로 재정렬
// 5. ResultSorter로 정렬 모드(RELEVANCE/POPULARITY/RECENT) 적용
// 6. PromptBuilder로 시스템 프롬프트 + 컨텍스트 + rewrittenQuery 조립
// 7. Spring AI ChatClient.prompt().stream()으로 Ollama(카나나) 스트리밍 호출
// 8. 스트림 종료 시 사용된 post_id 목록을 별도 이벤트로 덧붙여 SSE 응답 구성
