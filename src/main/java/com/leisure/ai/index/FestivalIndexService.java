package com.leisure.ai.index;

import com.leisure.ai.embedding.TextPreprocessor;
import com.leisure.ai.index.dto.FestivalIndexRequest;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service //축제 임베딩
public class FestivalIndexService {
    private final VectorStore festivalsVectorStore;
    private final TextPreprocessor textPreprocessor;

    public FestivalIndexService(@Qualifier("festivalsVectorStore") VectorStore festivalsVectorStore,TextPreprocessor textPreprocessor) {
        this.festivalsVectorStore = festivalsVectorStore;
        this.textPreprocessor = textPreprocessor;
    }

    // 축제 추가/수정
    public void upsert(FestivalIndexRequest request) {
        festivalsVectorStore.add(List.of(toDocument(request)));
    }

    // 전체 재색인(ReindexService)용 
    public void upsertAll(List<FestivalIndexRequest> requests) {
        festivalsVectorStore.add(requests.stream().map(this::toDocument).toList());
    }

    // 축제 삭제
    public void delete(Long festivalId) {
        festivalsVectorStore.delete(List.of(String.valueOf(festivalId)));
    }

    // 축제 임베딩용 Document 변환
    private Document toDocument(FestivalIndexRequest request) {
        String embeddingText = textPreprocessor.assembleFestivalText(
                request.title(), request.description(), request.region(), request.address());

        return Document.builder()
                .id(String.valueOf(request.festivalId()))
                .text(embeddingText)
                .metadata(toPayload(request))
                .build();
    }

    // 축제 임베딩용 Document의 metadata 변환 
    private Map<String, Object> toPayload(FestivalIndexRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("festival_id", request.festivalId());
        payload.put("title", request.title());
        payload.put("description", request.description());
        payload.put("address", request.address());
        payload.put("region", request.region());
        payload.put("start_date", request.startDate());
        payload.put("end_date", request.endDate());
        return payload;
    }
}
