package com.leisure.ai.chat.pipeline;

import com.leisure.ai.chat.dto.QueryIntent;
import com.leisure.ai.chat.dto.SearchFilter;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component // 어떤 컬렉션에서 검사할지 리라이터가 판단한 결과를 기반으로, 실제 검색 시 어떤 컬렉션을 선택할지 결정하는 역할
public class CollectionRouter {
    public enum Collection { POSTS, FESTIVALS }
    public record Route(Collection collection, SearchFilter filter) {}

    public Route route(QueryIntent intent, SearchFilter baseFilter) {
        if (intent.isFestivalQuery()) {
            // 축제 payload엔 status 필드 자체가 없어서 null로 둠 (SearchFilterMapper 참고).
            // 대신 종료된 축제는 제외하도록 end_date >= 오늘 조건을 채움.
            SearchFilter festivalFilter = new SearchFilter(
                    baseFilter.region(), null, Instant.now().getEpochSecond());
            return new Route(Collection.FESTIVALS, festivalFilter);
        }
        return new Route(Collection.POSTS, baseFilter);
    }
}
