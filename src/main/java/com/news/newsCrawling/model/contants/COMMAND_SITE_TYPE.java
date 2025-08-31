package com.news.newsCrawling.model.contants;

import lombok.Getter;

@Getter
public enum COMMAND_SITE_TYPE {

    DAUM("DAUM", "daum");

    private final String value;
    private final String message;

    COMMAND_SITE_TYPE(String value, String message) {
        this.value = value;
        this.message = message;
    }
}
