package com.leisure.ai.vector;

// TODO (로직 문서 C. 초기화 1~2단계)
// - ApplicationRunner 또는 @PostConstruct로 기동 시 실행
// - Spring AI가 자동 등록해주는 QdrantClient(저수준) 빈을 직접 사용
//   (VectorStore 인터페이스에는 컬렉션 생성/payload 인덱스 기능이 없음)
// - posts, festivals 컬렉션이 없으면 생성 (vector size 1024, distance cosine)
// - 필터 성능을 위한 payload 인덱스 생성: region, category, status, end_date
// - 이미 존재하면 스킵 (멱등하게)
