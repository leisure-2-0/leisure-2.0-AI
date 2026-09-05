package com.leisure.ai.chat.pipeline;

import com.leisure.ai.embedding.RerankerModel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class Reranker {
    private static final int TOP_K = 5;
    private final RerankerModel rerankerModel;
    public Reranker(RerankerModel rerankerModel) {this.rerankerModel = rerankerModel;}

    public Mono<List<ScoredDocument>> rerank(String rewrittenQuery, List<ScoredDocument> candidates) {
        // ONNX 추론은 블로킹 호출이라 별도 스케줄러로 옮겨서 리액티브 체인을 안 막음
        return Mono.fromCallable(() -> doRerank(rewrittenQuery, candidates)).subscribeOn(Schedulers.boundedElastic());
    }

    private List<ScoredDocument> doRerank(String rewrittenQuery, List<ScoredDocument> candidates) {
        List<String> texts = candidates.stream().map(this::documentText).toList();
        List<Double> scores = rerankerModel.score(rewrittenQuery, texts);
        List<ScoredDocument> reranked = new ArrayList<>(candidates.size());
        
        for (int i = 0; i < candidates.size(); i++) {
            // 리랭킹 점수로 교체 (Retriever가 매긴 유사도 점수는 여기서부터 더 안 씀)
            reranked.add(new ScoredDocument(candidates.get(i).document(), scores.get(i)));
        }

        return reranked.stream().sorted(Comparator.comparingDouble(ScoredDocument::score).reversed()).limit(TOP_K).toList();
    }

    private String documentText(ScoredDocument scoredDocument) {
        var metadata = scoredDocument.document().getMetadata();
        Object title = metadata.get("title");
        Object content = metadata.get("content");
        return title + "\n" + content;
    }
}
