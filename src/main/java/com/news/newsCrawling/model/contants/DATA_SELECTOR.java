package com.news.newsCrawling.model.contants;

import lombok.Getter;

@Getter
public enum DATA_SELECTOR{
    WRAPPER("wrapper"),
    TITLE("title"),
    CONTENT("content"),
    PUBLISHER("publisher"),
    CREATED_AT("createdAt"),
    COMMENTS("comments"),
    EMOTICON("emoticon"),
    URL("url"),
    DEPTH1("depth1"),
    DEPTH2("depth2");
    private final String value;

    DATA_SELECTOR(String value) {
        this.value = value;
    }
}
