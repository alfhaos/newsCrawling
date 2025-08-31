package com.news.newsCrawling.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "crawling")
@Data
public class CrawlingSiteConfig {
    private Map<String, Site> sites;

    @Data
    public static class Site {
        private String url;
        private LinkedHashMap<String, String> selectors;
        private LinkedHashMap<String, String> dataSelectors;
    }
}
