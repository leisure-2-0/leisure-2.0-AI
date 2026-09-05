package com.leisure.ai.global.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// spring-ai-starter-vector-store-qdrant의 기본 자동 설정은 application.yml에서 꺼뒀음
// (컬렉션 1개 가정이라 posts/festivals 2개가 필요한 우리 구조와 안 맞아서). 그래서 QdrantClient부터
// VectorStore 빈 2개까지 여기서 직접 구성함.
@Configuration
public class VectorStoreConfig {
    @Bean
    public QdrantClient qdrantClient(
            @Value("${spring.ai.vectorstore.qdrant.host:localhost}") String host,
            @Value("${spring.ai.vectorstore.qdrant.port:6334}") int port,
            @Value("${spring.ai.vectorstore.qdrant.use-tls:false}") boolean useTls,
            @Value("${spring.ai.vectorstore.qdrant.api-key:}") String apiKey) {
        QdrantGrpcClient.Builder builder = QdrantGrpcClient.newBuilder(host, port, useTls);
        if (!apiKey.isBlank()) {
            builder.withApiKey(apiKey);
        }
        return new QdrantClient(builder.build());
    }

    @Bean
    public VectorStore postsVectorStore(QdrantClient qdrantClient, EmbeddingModel embeddingModel) {
        return QdrantVectorStore.builder(qdrantClient, embeddingModel)
                .collectionName("posts")
                .initializeSchema(false) // 컬렉션 생성/payload 인덱스는 vector.CollectionInitializer가 담당
                .build();
    }

    @Bean
    public VectorStore festivalsVectorStore(QdrantClient qdrantClient, EmbeddingModel embeddingModel) {
        return QdrantVectorStore.builder(qdrantClient, embeddingModel)
                .collectionName("festivals")
                .initializeSchema(false)
                .build();
    }
}
