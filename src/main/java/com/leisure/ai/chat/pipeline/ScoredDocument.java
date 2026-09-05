package com.leisure.ai.chat.pipeline;

import com.leisure.ai.vector.QdrantIds;
import org.springframework.ai.document.Document;

// Retriever -> Reranker -> ResultSorter -> PromptBuilder 사이에서 공유하는 중간 표현.
// score의 의미는 단계별로 달라짐 (Retriever 직후: 유사도, Reranker 이후: 리랭킹 점수).
public record ScoredDocument(Document document, double score) {

    // 인덱싱 시 Document id = QdrantIds.toPointId(postId/festivalId) 로 저장한다는 전제
    public Long id() {
        return QdrantIds.fromPointId(document.getId());
    }
}
