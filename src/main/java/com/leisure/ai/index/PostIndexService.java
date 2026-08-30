package com.leisure.ai.index;

// TODO (로직 문서 A-1, A-2, A-3)
// - upsert: TextPreprocessor로 제목+본문+태그명 조립 -> BgeM3EmbeddingModel 임베딩
//   -> postsVectorStore에 Document(id=postId, content, metadata=payload 전체) upsert
// - 수정 시: 본문/태그 변경이든 제목만 변경이든 임베딩 텍스트에 제목이 포함되므로 항상 재임베딩
// - 삭제/비승인 시: postsVectorStore.delete(List.of(postId))
