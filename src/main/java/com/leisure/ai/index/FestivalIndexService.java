package com.leisure.ai.index;

// TODO (로직 문서 A-5)
// - upsert: 자연문 조립 -> 임베딩 -> festivalsVectorStore에 upsert
//   포인트 id는 TourAPI contentId를 Long으로 파싱해서 사용 (Qdrant 포인트 id는 unsigned int/UUID만 허용,
//   문자열 그대로 넣으면 에러)
// - 삭제: 종료 30일 경과 축제 -> festivalsVectorStore.delete(List.of(festivalId))
