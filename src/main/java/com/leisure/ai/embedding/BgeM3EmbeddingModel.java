package com.leisure.ai.embedding;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import jakarta.annotation.PreDestroy;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// BGE-M3를 ONNX Runtime으로 직접 돌리는 Spring AI EmbeddingModel 구현체.
// VectorStoreConfig가 만드는 QdrantVectorStore들이 이 빈을 그대로 주입받아 씀.
@Component
public class BgeM3EmbeddingModel implements EmbeddingModel {

    // CollectionInitializer가 컬렉션 생성 시 이 값을 그대로 참조함 (벡터 차원 불일치 방지)
    public static final int DIMENSIONS = 1024;
    private static final int MAX_LENGTH = 8192;

    private final OrtEnvironment environment;
    private final OrtSession session;
    private final HuggingFaceTokenizer tokenizer;

    public BgeM3EmbeddingModel(OrtEnvironment environment, @Value("${onnx.models.embedding-path:models/bge-m3}") String modelDir) {
        this.environment = environment;
        Path dir = Path.of(modelDir);
        
        try {
            this.session = environment.createSession(
                    dir.resolve("model.onnx").toString(), new OrtSession.SessionOptions());
            this.tokenizer = HuggingFaceTokenizer.builder()
                    .optTokenizerPath(dir.resolve("tokenizer.json"))
                    .optAddSpecialTokens(true)
                    .optTruncation(true)
                    .optMaxLength(MAX_LENGTH)
                    .optPadding(true) // 배치 내 최장 길이에 맞춰 동적 패딩
                    .build();
        } catch (OrtException | IOException e) {
            throw new IllegalStateException("BGE-M3 ONNX 모델 로드 실패 (경로: " + modelDir + ")", e);
        }
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<float[]> vectors = embedBatch(request.getInstructions());
        List<Embedding> embeddings = new ArrayList<>(vectors.size());
        for (int i = 0; i < vectors.size(); i++) {
            embeddings.add(new Embedding(vectors.get(i), i));
        }
        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        return embedBatch(List.of(document.getText())).get(0);
    }

    @Override
    public int dimensions() {
        return DIMENSIONS;
    }

    private List<float[]> embedBatch(List<String> texts) {
        Encoding[] encodings = tokenizer.batchEncode(texts);
        int batchSize = encodings.length;

        long[][] inputIds = new long[batchSize][];
        long[][] attentionMask = new long[batchSize][];
        for (int i = 0; i < batchSize; i++) {
            inputIds[i] = encodings[i].getIds();
            attentionMask[i] = encodings[i].getAttentionMask();
        }

        // XLM-RoBERTa 계열(BGE-M3)은 보통 token_type_ids가 필요 없음.
        // 실제 onnx 파일이 이걸 요구하면(session.getInputNames()로 확인) 0-텐서를 추가해야 함.
        try (OnnxTensor inputIdsTensor = OnnxTensor.createTensor(environment, inputIds);
             OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(environment, attentionMask);
             OrtSession.Result result = session.run(Map.of(
                     "input_ids", inputIdsTensor,
                     "attention_mask", attentionMaskTensor))) {
            return extractEmbeddings(result, batchSize);
        } catch (OrtException e) {
            throw new IllegalStateException("BGE-M3 ONNX 추론 실패", e);
        }
    }

    private List<float[]> extractEmbeddings(OrtSession.Result result, int batchSize) throws OrtException {
        // 실제 모델 파일을 넣고 session.getOutputNames()로 정확한 출력 이름/형태를 먼저 확인할 것.
        // 아래는 가장 흔한 두 케이스(export 방식에 따라 다름)에 대한 기본 처리.
        var pooled = result.get("sentence_embedding");
        if (pooled.isPresent()) {
            // export 단계에서 이미 풀링+정규화까지 끝난 문장 임베딩을 직접 내보내는 경우
            float[][] vectors = (float[][]) pooled.get().getValue();
            return normalizeAll(vectors);
        }

        // 아니면 last_hidden_state([batch, seq, hidden])에서 CLS 토큰(0번 위치)만 뽑아 L2 정규화 (BGE 공식 추론 방식)
        float[][][] lastHiddenState = (float[][][]) result.get(0).getValue();
        float[][] cls = new float[batchSize][];
        for (int i = 0; i < batchSize; i++) {
            cls[i] = lastHiddenState[i][0];
        }
        return normalizeAll(cls);
    }

    private List<float[]> normalizeAll(float[][] vectors) {
        List<float[]> result = new ArrayList<>(vectors.length);
        for (float[] vector : vectors) {
            result.add(l2Normalize(vector));
        }
        return result;
    }

    private float[] l2Normalize(float[] vector) {
        double sumSquares = 0;
        for (float v : vector) {
            sumSquares += (double) v * v;
        }
        double norm = Math.sqrt(sumSquares);
        if (norm == 0) {
            return vector;
        }
        float[] normalized = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalized[i] = (float) (vector[i] / norm);
        }
        return normalized;
    }

    @PreDestroy
    public void close() {
        try {
            session.close();
        } catch (OrtException ignored) {
            // 종료 시 리소스 정리 실패는 무시
        }
        tokenizer.close();
    }
}
