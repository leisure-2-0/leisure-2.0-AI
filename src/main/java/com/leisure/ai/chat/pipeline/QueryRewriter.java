package com.leisure.ai.chat.pipeline;

import com.leisure.ai.chat.dto.ChatRequest;
import com.leisure.ai.chat.dto.QueryIntent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.stream.Collectors;

@Component
public class QueryRewriter {

    private static final String SYSTEM = """
            대화 이력과 새 질문을 보고, 검색에 쓸 독립적인 질문으로 다시 써라(rewrittenQuery).
            정렬 의도가 있으면 sortMode를 RELEVANCE/POPULARITY/RECENT 중 하나로 판단하고, 없으면 RELEVANCE로 두어라.
            질문이 축제/행사에 관한 것이면 isFestivalQuery를 true로, 아니면 false로 판단하라.
            """;

    private final ChatClient chatClient;

    public QueryRewriter(ChatClient.Builder chatClientBuilder) {this.chatClient = chatClientBuilder.build();}

    public Mono<QueryIntent> rewrite(ChatRequest request) {
        // history가 없어도 isFestivalQuery/sortMode 판단은 필요하므로 LLM 호출은 항상 수행.
        // 블로킹 호출하지 않도록 boundedElastic 스케줄러에서 실행하도록 처리
        return Mono.fromCallable(() -> rewriteWithLlm(request))
                .subscribeOn(Schedulers.boundedElastic())
                // LLM 호출 실패든, 구조화 출력 파싱 실패든 전부 폴백으로 흡수
                .onErrorReturn(QueryIntent.fallback(request.question()));
    }

    // 대화 이력을 하나로 합치기 (없으면 빈 문자열)
    private QueryIntent rewriteWithLlm(ChatRequest request) {
        String history = request.history() == null ? "" : request.history().stream()
                .map(turn -> turn.role() + ": " + turn.content())
                .collect(Collectors.joining("\n"));

        return chatClient.prompt()
                .system(SYSTEM)
                .user("""
                        [대화 이력]
                        %s

                        [새 질문]
                        %s
                        """.formatted(history, request.question()))
                .call()
                .entity(QueryIntent.class);
    }
}
