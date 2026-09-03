package com.leisure.ai.index;

import com.leisure.ai.embedding.TextPreprocessor;
import com.leisure.ai.index.dto.PostIndexRequest;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service // 게시글 임베딩 
public class PostIndexService {
    private final VectorStore postsVectorStore;
    private final TextPreprocessor textPreprocessor;

    public PostIndexService(@Qualifier("postsVectorStore") VectorStore postsVectorStore,TextPreprocessor textPreprocessor) {
        this.postsVectorStore = postsVectorStore;
        this.textPreprocessor = textPreprocessor;
    }

    // 게시글 추가, 수정 _ 게시글이 수정된 경우, 항상 재 임베딩을 진행하도록 함. 
    public void upsert(PostIndexRequest request) {
        postsVectorStore.add(List.of(toDocument(request)));
    }

    // 전체 재색인(ReindexService)용 
    public void upsertAll(List<PostIndexRequest> requests) {
        postsVectorStore.add(requests.stream().map(this::toDocument).toList());
    }

    // 삭제
    public void delete(Long postId) {
        postsVectorStore.delete(List.of(String.valueOf(postId)));
    }

    // 게시글 임베딩용 Document 변환
    private Document toDocument(PostIndexRequest request) {
        String embeddingText = textPreprocessor.assemblePostText(
                request.title(), request.content(), request.tags());

        return Document.builder()
                .id(String.valueOf(request.postId()))
                .text(embeddingText)
                .metadata(toPayload(request))
                .build();
    }

    // 게시글 임베딩용 Document의 metadata 변환
    private Map<String, Object> toPayload(PostIndexRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("post_id", request.postId());
        payload.put("title", request.title());
        payload.put("content", request.content());
        payload.put("region", request.region());
        payload.put("region_detail", request.regionDetail());
        payload.put("category", request.category());
        payload.put("tags", request.tags());
        payload.put("status", "APPROVED"); // 이 서비스로 upsert되는 글은 항상 승인된 상태
        payload.put("view_count", request.viewCount() == null ? 0L : request.viewCount());
        payload.put("like_count", request.likeCount() == null ? 0L : request.likeCount());
        payload.put("created_at", request.createdAt());
        return payload;
    }
}
