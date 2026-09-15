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

    private static final String SYSTEM_TEMPLATE = """
            대화 이력과 새 질문을 보고, 검색에 쓸 독립적인 질문으로 다시 써라(rewrittenQuery).
            정렬 의도가 있으면 sortMode를 RELEVANCE/POPULARITY/RECENT 중 하나로 판단하고, 없으면 RELEVANCE로 두어라.
            질문이 축제/행사에 관한 것이면 isFestivalQuery를 true로, 아니면 false로 판단하라.
            질문에 특정 지역이 언급되면, 아래 [지역 목록]에서 정확히 일치하는 이름 하나를 골라 region에 채워라.
            목록에 없는 지역명을 만들어내지 말고, 지역 언급이 없거나 목록에서 찾을 수 없으면 region은 null로 두어라.
            [지역 목록]
            %s
            """;

    private final ChatClient chatClient;
    private final String system;

    public QueryRewriter(ChatClient.Builder chatClientBuilder, RegionWhitelist regionWhitelist) {
        this.chatClient = chatClientBuilder.build();
        this.system = SYSTEM_TEMPLATE.formatted(regionWhitelist.joined());
    }

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
                .system(system)
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
