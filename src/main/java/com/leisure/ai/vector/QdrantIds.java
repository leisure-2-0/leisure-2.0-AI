package com.leisure.ai.vector;

import java.util.UUID;

// Spring AI의 QdrantVectorStore.doAdd()/doDelete()가 Document id를 UUID.fromString()으로
// 파싱하도록 하드코딩돼 있어서(숫자 문자열 그대로는 안 받음), postId/festivalId(Long)를
// 결정론적으로 UUID처럼 생긴 문자열로 변환해서 씀. 같은 long 값은 항상 같은 UUID가 나오고,
// 다시 되돌릴 수도 있음(fromPointId). posts/festivals는 서로 다른 컬렉션이라 충돌 걱정 없음.
public final class QdrantIds {

    public static UUID toUuid(long id) {
        return new UUID(0L, id);
    }

    public static String toPointId(long id) {
        return toUuid(id).toString();
    }

    public static long fromPointId(String pointId) {
        return UUID.fromString(pointId).getLeastSignificantBits();
    }

    private QdrantIds() {
    }
}
