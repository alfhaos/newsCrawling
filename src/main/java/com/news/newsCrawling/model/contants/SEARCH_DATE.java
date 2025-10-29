package com.news.newsCrawling.model.contants;

public enum SEARCH_DATE {

    DAILY("DAILY", "daily"),

    WEEKLY("WEEKLY", "weekly");


    private final String value;
    private final String message;

    SEARCH_DATE(String value, String message) {
        this.value = value;
        this.message = message;
    }
}
