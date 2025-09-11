package com.news.newsCrawling.model.contants;

public enum AGENT_ROLE {

    MAIN("MAIN", "main"),
    WORKER("WORKER", "worker");


    private final String value;
    private final String message;

    AGENT_ROLE(String value, String message) {
        this.value = value;
        this.message = message;
    }
}
