package com.leisure.ai.vector;

import com.leisure.ai.chat.dto.SearchFilter;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder.Op;
import org.springframework.stereotype.Component;

// SearchFilter에서 null이 아닌 필드만 AND로 묶어서 Qdrant 필터 표현식으로 변환.
// 어떤 필드를 채워서 넘길지는 CollectionRouter 책임 - 여기는 받은 값을 그대로 믿고 변환만 함.
@Component
public class SearchFilterMapper {
    private final FilterExpressionBuilder builder = new FilterExpressionBuilder();

    // 필터링할 조건이 하나도 없으면 null 반환 (호출 측에서 filterExpression 자체를 생략해야 함)
    public Filter.Expression toExpression(SearchFilter filter) {
        Op combined = null;

        if (filter.region() != null) {combined = builder.eq("region", filter.region());}
        if (filter.endDateAfter() != null) {combined = and(combined, builder.gte("end_date", filter.endDateAfter()));}

        return combined == null ? null : combined.build();
    }

    private Op and(Op left, Op right) {
        return left == null ? right : builder.and(left, right);
    }
}
