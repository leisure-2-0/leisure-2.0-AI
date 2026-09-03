package com.leisure.ai.chat.pipeline;

import com.leisure.ai.vector.SearchFilterMapper;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Component // 검색을 담당하는 컴포넌트. 리라이팅된 쿼리와 컬렉션 라우팅 결과를 받아서, 실제 검색을 수행하고, 점수화된 문서 리스트를 반환
public class Retriever {
    private static final int TOP_K = 10;

    private final VectorStore postsVectorStore;
    private final VectorStore festivalsVectorStore;
    private final SearchFilterMapper searchFilterMapper;

    public Retriever(@Qualifier("postsVectorStore") VectorStore postsVectorStore,
                      @Qualifier("festivalsVectorStore") VectorStore festivalsVectorStore,
                      SearchFilterMapper searchFilterMapper) {
        this.postsVectorStore = postsVectorStore;
        this.festivalsVectorStore = festivalsVectorStore;
        this.searchFilterMapper = searchFilterMapper;
    }

    // 검색 수행
    public Mono<List<ScoredDocument>> search(String rewrittenQuery, CollectionRouter.Route route) {
        // VectorStore.similaritySearch는 블로킹 호출이라 별도 스케줄러로 옮겨서 리액티브 체인을 안 막음
        return Mono.fromCallable(() -> doSearch(rewrittenQuery, route))
                .subscribeOn(Schedulers.boundedElastic());
    }

    // 실제 검색 로직
    private List<ScoredDocument> doSearch(String rewrittenQuery, CollectionRouter.Route route) {
        VectorStore vectorStore = route.collection() == CollectionRouter.Collection.FESTIVALS ? festivalsVectorStore : postsVectorStore;

        SearchRequest.Builder requestBuilder = SearchRequest.builder().query(rewrittenQuery).topK(TOP_K);
        Filter.Expression filterExpression = searchFilterMapper.toExpression(route.filter());

        if (filterExpression != null) { requestBuilder.filterExpression(filterExpression);}

        List<Document> documents = vectorStore.similaritySearch(requestBuilder.build());

        return documents.stream().map(document -> new ScoredDocument(document, score(document))).toList();
    }

    // 점수화된 문서 생성
    private double score(Document document) {
        return document.getScore() == null ? 0.0 : document.getScore();
    }
}
