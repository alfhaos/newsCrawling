package com.news.newscrawling.util;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommonResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public CommonResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
    public CommonResponse(T data) {
        this.success = true;
        this.message = "Success";
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

}
