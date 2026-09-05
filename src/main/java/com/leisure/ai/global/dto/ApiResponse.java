package com.leisure.ai.global.dto;

// 인덱싱 API처럼 단순 성공/실패만 필요한 응답에 사용.
public record ApiResponse<T>(boolean success, T data, String error) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message);
    }
}
