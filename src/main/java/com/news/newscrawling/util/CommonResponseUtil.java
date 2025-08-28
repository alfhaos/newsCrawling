package com.news.newscrawling.util;

public class CommonResponseUtil {

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(true, "Request processed successfully", data);
    }

    public static <T> CommonResponse<T> success(String message, T data) {
        return new CommonResponse<>(true, message, data);
    }

    public static CommonResponse<Object> failure() {
        return new CommonResponse<>(false, "failed", null);
    }

    public static CommonResponse<Object> failure(Object data) {
        return new CommonResponse<>(false, "failed", data);
    }
}
