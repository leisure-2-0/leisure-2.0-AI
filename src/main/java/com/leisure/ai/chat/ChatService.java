package com.leisure.ai.chat;

import com.leisure.ai.chat.dto.ChatRequest;
import com.leisure.ai.chat.dto.ChatResponse;
import com.leisure.ai.chat.pipeline.CollectionRouter;
import com.leisure.ai.chat.pipeline.PromptBuilder;
import com.leisure.ai.chat.pipeline.QueryRewriter;
import com.leisure.ai.chat.pipeline.Reranker;
import com.leisure.ai.chat.pipeline.Retriever;
import com.leisure.ai.chat.pipeline.ResultSorter;
import com.leisure.ai.chat.pipeline.ScoredDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final QueryRewriter queryRewriter;
    private final CollectionRouter collectionRouter;
    private final Retriever retriever;
    private final Reranker reranker;
    private final ResultSorter resultSorter;
    private final PromptBuilder promptBuilder;
    private final ChatClient chatClient;

    public ChatService(QueryRewriter queryRewriter,CollectionRouter collectionRouter,Retriever retriever,
                        Reranker reranker,ResultSorter resultSorter,PromptBuilder promptBuilder,ChatClient.Builder chatClientBuilder) {
        this.queryRewriter = queryRewriter;
        this.collectionRouter = collectionRouter;
        this.retriever = retriever;
        this.reranker = reranker;
        this.resultSorter = resultSorter;
        this.promptBuilder = promptBuilder;
        this.chatClient = chatClientBuilder.build();
    }

    // Flux -> Reactor 라이브러리에서 제공하는 타입 :: 0개 ~ 여러 개의 데이터를 시간에 따라 흘려보내는 컨테이너
    // Mono -> Reactor 라이브러리에서 제공하는 타입 :: 0개 ~ 1개의 데이터를 시간에 따라 흘려보내는 컨테이너
    // 토큰이 생성되는대로 전달하기 위해서 사용
    public Flux<ChatResponse> streamAnswer(ChatRequest request) {
        log.info("[chat] 질문 수신: \"{}\"", request.question());

        // 리라이팅 결과를 토큰 스트림 해야하기 때문에 -> Mono에서 Flux로 변환(flatMapMany) 필요
        return queryRewriter.rewrite(request)
                .doOnNext(intent -> log.info("[chat] 1) 리라이팅 완료: rewrittenQuery=\"{}\" sortMode={} isFestivalQuery={}",
                        intent.rewrittenQuery(), intent.sortMode(), intent.isFestivalQuery()))
                .flatMapMany(intent -> {
                    // 컬렉션 선택 + 조건 추출
                    CollectionRouter.Route route = collectionRouter.route(intent);
                    log.info("[chat] 2) 컬렉션 라우팅: {} filter={}", route.collection(), route.filter());

                    // 검색 -> 재정렬 -> 정렬 -> 응답 생성
                    return retriever.search(intent.rewrittenQuery(), route)
                            .doOnNext(candidates -> log.info("[chat] 3) 검색 결과: {}건", candidates.size()))
                            .flatMap(candidates -> reranker.rerank(intent.rewrittenQuery(), candidates))
                            .doOnNext(reranked -> log.info("[chat] 4) 리랭킹 완료: {}건, 상위 점수={}",
                                    reranked.size(), reranked.isEmpty() ? "-" : reranked.get(0).score()))
                            .map(reranked -> resultSorter.sort(reranked, intent.sortMode()))
                            .doOnNext(sorted -> log.info("[chat] 5) 정렬 완료: {}건 (모드={})", sorted.size(), intent.sortMode()))
                            .flatMapMany(sorted -> respond(intent.rewrittenQuery(), sorted));
                })
                .doOnError(e -> log.error("[chat] 파이프라인 처리 중 오류", e));
    }

    // 응답 생성
    private Flux<ChatResponse> respond(String rewrittenQuery, List<ScoredDocument> documents) {
        // 리라이팅 프롬프트
        Prompt prompt = promptBuilder.build(rewrittenQuery, documents);
        // 사용된 문서 ID 추출
        List<Long> usedIds = documents.stream().map(ScoredDocument::id).toList();
        log.info("[chat] 6) LLM 답변 생성 시작 (참고 문서 id={})", usedIds);

        // ChatClient를 통해 프롬프트를 전달하고, 토큰 단위로 응답을 스트리밍
        Flux<ChatResponse> tokens = chatClient.prompt(prompt).stream().content().map(ChatResponse.Token::new);

        return Flux.concat(tokens, Flux.just(new ChatResponse.Done(usedIds)))
                .doOnComplete(() -> log.info("[chat] 7) 응답 스트림 종료"));
    }
}
