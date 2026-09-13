package com.leisure.ai.chat.pipeline;

import com.leisure.ai.chat.dto.QueryIntent;
import com.leisure.ai.chat.dto.SearchFilter;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component // 어떤 컬렉션에서 검사할지 리라이터가 판단한 결과를 기반으로, 실제 검색 시 어떤 컬렉션을 선택할지 결정하는 역할
public class CollectionRouter {
    public enum Collection { POSTS, FESTIVALS }
    public record Route(Collection collection, SearchFilter filter) {}

    public Route route(QueryIntent intent) {
        if (intent.isFestivalQuery()) {
            // 축제는 메인 백엔드에 /ai/index/festival 색인 호출 자체가 아직 없어서, 색인될 region 값이
            // 게시글(카카오 region_2depth_name)과 같은 포맷일지 확정되지 않았다. 포맷이 정해지기 전까지는
            // region 필터를 걸지 않고 종료된 축제만 제외한다(end_date >= 오늘). 포맷이 정해지면 아래 주석 해제.
            // SearchFilter festivalFilter = new SearchFilter(intent.region(), Instant.now().getEpochSecond());
            SearchFilter festivalFilter = new SearchFilter(null, Instant.now().getEpochSecond());
            return new Route(Collection.FESTIVALS, festivalFilter);
        }
        return new Route(Collection.POSTS, new SearchFilter(intent.region(), null));
    }
}
