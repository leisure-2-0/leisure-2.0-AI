package com.leisure.ai.chat.pipeline;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

// region 추출을 LLM이 자유롭게 하도록 두면 Qdrant payload의 실제 값(카카오 region_2depth_name)과
// 표기가 어긋나 정확매칭(eq) 필터가 0건으로 실패할 수 있다. 그래서 실제로 쓰이는 시군구명 목록을
// 화이트리스트로 두고 LLM이 그 안에서만 고르게 한다. 목록 출처: 메인 백엔드 regions 테이블(TourAPI 동기화).
@Component
public class RegionWhitelist {

    private final List<String> values;
    private final String joined;

    public RegionWhitelist() {
        this.values = load();
        this.joined = String.join(", ", values);
    }

    public List<String> values() {
        return values;
    }

    // QueryRewriter 시스템 프롬프트에 그대로 삽입할 콤마 구분 문자열
    public String joined() {
        return joined;
    }

    private List<String> load() {
        try (var reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("regions/signgu-names.txt").getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().map(String::trim).filter(line -> !line.isEmpty()).toList();
        } catch (IOException e) {
            throw new IllegalStateException("지역 화이트리스트(regions/signgu-names.txt) 로드 실패", e);
        }
    }
}
