package com.leisure.ai.index;

import com.leisure.ai.embedding.TextPreprocessor;
import com.leisure.ai.index.dto.FestivalIndexRequest;
import com.leisure.ai.vector.QdrantIds;
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
        festivalsVectorStore.delete(List.of(QdrantIds.toPointId(festivalId)));
    }

    // 축제 임베딩용 Document 변환
    private Document toDocument(FestivalIndexRequest request) {
        String embeddingText = textPreprocessor.assembleFestivalText(
                request.title(), request.description(), request.region(), request.address());

        return Document.builder()
                .id(QdrantIds.toPointId(request.festivalId()))
                .text(embeddingText)
                .metadata(toPayload(request))
                .build();
    }

    // 축제 임베딩용 Document의 metadata 변환
    // Spring AI QdrantValueFactory가 Long은 문자열로, Integer는 숫자로 직렬화하는 차이가 있어서
    // (범위 필터가 걸리는 날짜/id 필드는) Integer로 캐스팅해서 넣음. 에폭초는 2038년까지는 int 범위 안에 들어옴.
    private Map<String, Object> toPayload(FestivalIndexRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("festival_id", request.festivalId().intValue());
        payload.put("title", request.title());
        payload.put("description", request.description());
        payload.put("address", request.address());
        payload.put("region", request.region());
        payload.put("start_date", request.startDate() == null ? null : request.startDate().intValue());
        payload.put("end_date", request.endDate().intValue());
        return payload;
    }
}
