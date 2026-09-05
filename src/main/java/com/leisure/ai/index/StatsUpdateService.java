package com.leisure.ai.index;

import com.leisure.ai.index.dto.StatsUpdateRequest;
import com.leisure.ai.vector.QdrantIds;
import io.qdrant.client.PointIdFactory;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.ValueFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

// 벡터 재계산 없이 payload(view_count, like_count)만 갱신.
// Spring AI의 VectorStore 인터페이스엔 payload 부분 갱신 API가 없어서 QdrantClient를 직접 씀.
@Service
public class StatsUpdateService {
    private static final String COLLECTION = "posts";
    private final QdrantClient qdrantClient;

    public StatsUpdateService(QdrantClient qdrantClient) {
        this.qdrantClient = qdrantClient;
    }

    public void update(List<StatsUpdateRequest> items) {
        for (StatsUpdateRequest item : items) {
            setPayload(item);
        }
    }

    private void setPayload(StatsUpdateRequest item) {
        Map<String, io.qdrant.client.grpc.JsonWithInt.Value> payload = Map.of(
                "view_count", ValueFactory.value(item.viewCount()),
                "like_count", ValueFactory.value(item.likeCount()));

        try {
            qdrantClient.setPayloadAsync(
                    COLLECTION,
                    payload,
                    PointIdFactory.id(QdrantIds.toUuid(item.postId())),
                    true,
                    null,
                    null).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Qdrant 갱신 중 인터럽트됨 (postId=" + item.postId() + ")", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Qdrant 갱신 실패 (postId=" + item.postId() + ")", e);
        }
    }
}
