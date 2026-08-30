package com.leisure.ai.chat.pipeline;

// TODO (로직 문서 10단계)
// - sortMode에 따라 최종 5건 정렬
//   RELEVANCE  -> 리랭킹 순서 그대로 유지
//   POPULARITY -> 리랭킹점수*0.5 + 정규화(like_count)*0.3 + 정규화(view_count)*0.2
//   RECENT     -> created_at 가중치 반영
// - 인기도 가중치 값은 "미결 사항" - 데이터 쌓인 후 튜닝 필요, 일단 상수로 빼두기
