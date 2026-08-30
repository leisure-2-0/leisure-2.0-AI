package com.leisure.ai.global.config;

// TODO
// - Spring AI가 자동 등록해주는 QdrantClient 빈(spring-ai-starter-vector-store-qdrant)과
//   embedding 패키지의 BgeM3EmbeddingModel 빈을 주입받아 QdrantVectorStore 빈을 "직접" 2개 생성
//   (posts, festivals - 우리 도메인은 컬렉션이 2개라 starter의 단일 컬렉션 자동 설정만으로는 부족)
//     @Bean @Qualifier("postsVectorStore") VectorStore postsVectorStore(QdrantClient, EmbeddingModel)
//         -> QdrantVectorStore.builder(...).collectionName("posts").build()
//     @Bean @Qualifier("festivalsVectorStore") VectorStore festivalsVectorStore(...)
//         -> collectionName("festivals")
// - initializeSchema는 false로 두고 실제 컬렉션 생성/payload 인덱스는
//   vector.CollectionInitializer에서 애플리케이션 기동 시 직접 처리 (분산 필터 성능 때문에
//   region/category/status/end_date 필드 인덱스가 필요한데, 이건 Spring AI가 안 해줌)
