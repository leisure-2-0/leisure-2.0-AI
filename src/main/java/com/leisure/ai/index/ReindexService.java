package com.leisure.ai.index;

// TODO (로직 문서 C. 초기화)
// - 전체 재색인: MySQL 대신 메인 백엔드가 넘겨주는 APPROVED 게시글 전체를 배치(100건) 단위로 임베딩 -> upsert
//   (이 서비스가 MySQL에 직접 접근하는지, 메인 백엔드가 벌크 API로 밀어주는지 아직 미정 - 확인 필요)
// - 축제 전체 동기화: FestivalIndexService.upsert를 전량에 대해 반복 호출
// - 재구축 필요 시점: 임베딩 모델 교체, 임베딩 텍스트 조립 방식 변경, Qdrant 장애 복구
