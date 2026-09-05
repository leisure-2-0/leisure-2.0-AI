package com.leisure.ai.index;

import com.leisure.ai.index.dto.FestivalIndexRequest;
import com.leisure.ai.index.dto.PostIndexRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;


// 메인 백엔드가 APPROVED 게시글/축제 전체를 벌크로 밀어준다고 전제하고, 여기서는 100건 단위로 나눠 upsert만 함.
@Service
public class ReindexService {

    private static final int BATCH_SIZE = 100;

    private final PostIndexService postIndexService;
    private final FestivalIndexService festivalIndexService;

    public ReindexService(PostIndexService postIndexService, FestivalIndexService festivalIndexService) {
        this.postIndexService = postIndexService;
        this.festivalIndexService = festivalIndexService;
    }

    public void reindexPosts(List<PostIndexRequest> posts) {
        chunk(posts, BATCH_SIZE).forEach(postIndexService::upsertAll);
    }

    public void reindexFestivals(List<FestivalIndexRequest> festivals) {
        chunk(festivals, BATCH_SIZE).forEach(festivalIndexService::upsertAll);
    }

    private <T> List<List<T>> chunk(List<T> items, int size) {
        int chunkCount = (items.size() + size - 1) / size;
        return IntStream.range(0, chunkCount)
                .mapToObj(i -> items.subList(i * size, Math.min((i + 1) * size, items.size())))
                .toList();
    }
}
