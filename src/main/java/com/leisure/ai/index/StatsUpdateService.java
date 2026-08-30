package com.leisure.ai.index;

// TODO (로직 문서 A-4)
// - 벡터 재계산 없이 payload(view_count, like_count)만 갱신
// - Spring AI VectorStore 인터페이스에는 payload-only 부분 갱신 API가 없을 가능성이 높음
//   -> QdrantClient(저수준, Spring AI가 자동 등록해주는 빈)를 직접 써서 setPayload 호출해야 할 수 있음
//   (VectorStoreConfig에서 QdrantClient 빈을 이 서비스에서도 주입받을 수 있게 확인)
