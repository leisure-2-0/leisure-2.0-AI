package com.leisure.ai.index;

import com.leisure.ai.global.dto.ApiResponse;
import com.leisure.ai.index.dto.FestivalIndexRequest;
import com.leisure.ai.index.dto.PostIndexRequest;
import com.leisure.ai.index.dto.StatsUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ai/index")
public class IndexController {
    private final PostIndexService postIndexService;
    private final FestivalIndexService festivalIndexService;
    private final StatsUpdateService statsUpdateService;
    private final ReindexService reindexService;

    public IndexController(PostIndexService postIndexService,FestivalIndexService festivalIndexService,
                            StatsUpdateService statsUpdateService,ReindexService reindexService) {
        this.postIndexService = postIndexService;
        this.festivalIndexService = festivalIndexService;
        this.statsUpdateService = statsUpdateService;
        this.reindexService = reindexService;
    }

    @PostMapping("/post") // 게시글 추가 / 수정
    public ApiResponse<Void> upsertPost(@Valid @RequestBody PostIndexRequest request) {
        postIndexService.upsert(request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/post/{postId}") // 게시글 삭제
    public ApiResponse<Void> deletePost(@PathVariable Long postId) {
        postIndexService.delete(postId);
        return ApiResponse.ok();
    }

    @PostMapping("/festival") // 축제 추가 / 수정
    public ApiResponse<Void> upsertFestival(@Valid @RequestBody FestivalIndexRequest request) {
        festivalIndexService.upsert(request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/festival/{festivalId}") // 축제 삭제
    public ApiResponse<Void> deleteFestival(@PathVariable Long festivalId) {
        festivalIndexService.delete(festivalId);
        return ApiResponse.ok();
    }

    @PostMapping("/stats") // 배치 업데이트 :: 게시글/축제 요청에 따라 처리
    public ApiResponse<Void> updateStats(@Valid @RequestBody List<StatsUpdateRequest> requests) {
        statsUpdateService.update(requests);
        return ApiResponse.ok();
    }

    @PostMapping("/reindex/posts") // 벡터 DB 최초 구축 시, 또는 재구축 시 호출
    public ApiResponse<Void> reindexPosts(@Valid @RequestBody List<PostIndexRequest> requests) {
        reindexService.reindexPosts(requests);
        return ApiResponse.ok();
    }

    @PostMapping("/reindex/festivals") // 벡터 DB 최초 구축 시, 또는 재구축 시 호출
    public ApiResponse<Void> reindexFestivals(@Valid @RequestBody List<FestivalIndexRequest> requests) {
        reindexService.reindexFestivals(requests);
        return ApiResponse.ok();
    }
}
