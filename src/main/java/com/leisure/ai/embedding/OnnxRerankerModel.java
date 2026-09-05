package com.leisure.ai.embedding;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.util.PairList;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// bge-reranker-v2-m3를 ONNX Runtime으로 직접 돌리는 크로스 인코더.
// BgeM3EmbeddingModel과 달리 (query, document) 쌍을 한 번에 인코딩해서 관련성 점수 하나를 뽑는 구조.
@Component
public class OnnxRerankerModel implements RerankerModel {

    // 질문+문서 쌍이라 임베딩 텍스트 하나보다는 짧게 잡음 (본문 전체가 아니라 쌍으로 들어가서 512면 충분히 넉넉)
    private static final int MAX_LENGTH = 512;

    private final OrtEnvironment environment;
    private final OrtSession session;
    private final HuggingFaceTokenizer tokenizer;

    public OnnxRerankerModel(OrtEnvironment environment,
                              @Value("${onnx.models.reranker-path:models/bge-reranker-v2-m3}") String modelDir) {
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
                    .optPadding(true)
                    .build();
        } catch (OrtException | IOException e) {
            throw new IllegalStateException("bge-reranker-v2-m3 ONNX 모델 로드 실패 (경로: " + modelDir + ")", e);
        }
    }

    @Override
    public List<Double> score(String query, List<String> documents) {
        if (documents.isEmpty()) {
            return List.of();
        }

        PairList<String, String> pairs = new PairList<>();
        for (String document : documents) {
            pairs.add(query, document);
        }
        Encoding[] encodings = tokenizer.batchEncode(pairs);

        long[][] inputIds = new long[encodings.length][];
        long[][] attentionMask = new long[encodings.length][];
        for (int i = 0; i < encodings.length; i++) {
            inputIds[i] = encodings[i].getIds();
            attentionMask[i] = encodings[i].getAttentionMask();
        }

        try (OnnxTensor inputIdsTensor = OnnxTensor.createTensor(environment, inputIds);
             OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(environment, attentionMask);
             OrtSession.Result result = session.run(Map.of(
                     "input_ids", inputIdsTensor,
                     "attention_mask", attentionMaskTensor))) {
            return extractScores(result);
        } catch (OrtException e) {
            throw new IllegalStateException("bge-reranker-v2-m3 ONNX 추론 실패", e);
        }
    }

    private List<Double> extractScores(OrtSession.Result result) throws OrtException {
        // 실제 모델 파일 넣고 session.getOutputNames()로 정확한 출력 이름 확인할 것.
        // 분류 헤드로 export된 크로스 인코더는 보통 "logits"([batch, 1]) 출력.
        var logitsOutput = result.get("logits");
        float[][] logits = logitsOutput.isPresent()
                ? (float[][]) logitsOutput.get().getValue()
                : (float[][]) result.get(0).getValue();

        List<Double> scores = new ArrayList<>(logits.length);
        for (float[] row : logits) {
            scores.add(sigmoid(row[0]));
        }
        return scores;
    }

    // 로짓을 0~1로 정규화 - ResultSorter의 인기도 가중합에서 다른 정규화 값들과 같은 스케일로 맞추기 위함
    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
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
