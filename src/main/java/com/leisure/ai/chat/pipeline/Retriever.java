package com.leisure.ai.chat.pipeline;

// TODO (로직 문서 7~8단계)
// - rewrittenQuery를 BgeM3EmbeddingModel로 임베딩 (Spring AI VectorStore가 내부적으로 호출해줌)
// - CollectionRouter가 정한 컬렉션에 맞는 VectorStore 빈(postsVectorStore/festivalsVectorStore) 선택
// - SearchFilter -> Filter.Expression 변환 후 similaritySearch(topK=10) 호출
