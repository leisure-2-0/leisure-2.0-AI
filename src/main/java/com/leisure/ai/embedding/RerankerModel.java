package com.leisure.ai.embedding;

import java.util.List;

// Spring AI에 대응하는 표준 인터페이스가 없어서 직접 정의
public interface RerankerModel {

    // query와 documents를 순서대로 짝지어 각각의 관련성 점수(0~1)를 반환. documents와 같은 길이/순서로 반환.
    List<Double> score(String query, List<String> documents);
}
