package com.news.newsCrawling.model.contants;

import lombok.Getter;

import java.io.Serializable;

@Getter
public enum COMMAND_SITE_TYPE implements Serializable {

    DAUM("DAUM", "daum"),
    RECURSIVE_DAUM("RECURSIVE_DAUM", "recursive_daum");

    private final String value;
    private final String message;

    COMMAND_SITE_TYPE(String value, String message) {
        this.value = value;
        this.message = message;
    }
}
