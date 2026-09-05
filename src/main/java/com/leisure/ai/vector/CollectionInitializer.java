package com.leisure.ai.vector;

import com.leisure.ai.embedding.BgeM3EmbeddingModel;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.PayloadSchemaType;
import io.qdrant.client.grpc.Collections.VectorParams;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@Component
public class CollectionInitializer implements ApplicationRunner {
    private final QdrantClient qdrantClient;
    public CollectionInitializer(QdrantClient qdrantClient) {this.qdrantClient = qdrantClient;}

    @Override // 컬렉션 존재 확인 후 없으면 생성 _ 생성시 페이로드 인덱스(검색 성능용) 함께 생성
    public void run(ApplicationArguments args) throws ExecutionException, InterruptedException {
        ensureCollection("posts", Map.of(
                "region", PayloadSchemaType.Keyword,
                "category", PayloadSchemaType.Keyword,
                "status", PayloadSchemaType.Keyword));

        ensureCollection("festivals", Map.of(
                "region", PayloadSchemaType.Keyword,
                "end_date", PayloadSchemaType.Integer));
    }

    private void ensureCollection(String collectionName, Map<String, PayloadSchemaType> indexedFields)
            throws ExecutionException, InterruptedException {
        boolean exists = qdrantClient.collectionExistsAsync(collectionName).get(); // 컬렉션 존재 여부 확인
        if (exists) {
            return;
        }

        // 컬렉션이 없으면 생성
        VectorParams vectorParams = VectorParams.newBuilder()
                .setSize(BgeM3EmbeddingModel.DIMENSIONS)
                .setDistance(Distance.Cosine)
                .build();
        qdrantClient.createCollectionAsync(collectionName, vectorParams).get();

        // 생성한 컬렉션에 받은 필드 목록마다 인덱스를 걸어줌
        for (Map.Entry<String, PayloadSchemaType> field : indexedFields.entrySet()) {
            qdrantClient.createPayloadIndexAsync(
                    collectionName, field.getKey(), field.getValue(), null, null, null, null).get();
        }
    }
}
