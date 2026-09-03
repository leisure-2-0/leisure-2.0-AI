package com.leisure.ai.chat.pipeline;

import com.leisure.ai.chat.dto.QueryIntent;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class ResultSorter {
    // 인기도 가중치 :: 이후 테스트해서 수정해야됨
    private static final double RELEVANCE_WEIGHT = 0.5;
    private static final double LIKE_WEIGHT = 0.3;
    private static final double VIEW_WEIGHT = 0.2;

    public List<ScoredDocument> sort(List<ScoredDocument> reranked, QueryIntent.SortMode sortMode) {
        return switch (sortMode) {
            case RELEVANCE -> reranked;
            case POPULARITY -> sortByPopularity(reranked);
            case RECENT -> sortByRecency(reranked);
        };
    }

    private List<ScoredDocument> sortByPopularity(List<ScoredDocument> documents) {
        double maxLike = documents.stream().mapToDouble(d -> metric(d, "like_count")).max().orElse(0);
        double maxView = documents.stream().mapToDouble(d -> metric(d, "view_count")).max().orElse(0);

        return documents.stream()
                .sorted(Comparator.comparingDouble((ScoredDocument d) ->
                                d.score() * RELEVANCE_WEIGHT
                                        + normalize(metric(d, "like_count"), maxLike) * LIKE_WEIGHT
                                        + normalize(metric(d, "view_count"), maxView) * VIEW_WEIGHT)
                        .reversed()).toList();
    }

    private List<ScoredDocument> sortByRecency(List<ScoredDocument> documents) {
        return documents.stream()
                .sorted(Comparator.comparingDouble((ScoredDocument d) -> metric(d, "created_at")).reversed()).toList();
    }

    private double normalize(double value, double max) {
        return max == 0 ? 0 : value / max;
    }

    private double metric(ScoredDocument document, String key) {
        Object value = document.document().getMetadata().get(key);
        return value instanceof Number number ? number.doubleValue() : 0;
    }
}
