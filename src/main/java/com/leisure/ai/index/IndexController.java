package com.leisure.ai.index;

// TODO
// - POST /ai/index/post       -> PostIndexService (게시글 승인/수정 시, upsert)
// - DELETE /ai/index/post/{id} -> PostIndexService (삭제/비승인 시, Qdrant 포인트 삭제)
// - POST /ai/index/stats      -> StatsUpdateService (조회수/좋아요 배치 갱신, payload만)
// - POST /ai/index/festival   -> FestivalIndexService (upsert)
// - DELETE /ai/index/festival/{id} -> FestivalIndexService (종료 30일 경과 축제 삭제)
// - 재색인 트리거 엔드포인트를 열지, CLI/배치 전용으로만 둘지 결정 필요
//   (열 경우 POST /ai/index/reindex, 관리자 전용이라 InternalAuthFilter로 충분한지 별도 권한 체크 필요한지 확인)
// - 모두 InternalAuthFilter 적용 대상
