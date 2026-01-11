package com.news.newsCrawling.model.contants;

public enum SEARCH_TYPE {
    TITLE("TITLE", "title"),
    CONTENT("CONTENT", "content"),
    EMOTICON("EMOTICON", "emoticon"),
    KEYWORD("KEYWORD", "keyword");

    private final String value;
    private final String message;

    SEARCH_TYPE(String value, String message) {
        this.value = value;
        this.message = message;
    }
}
