package com.news.newscrawling.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "crawling")
@Data
public class CrawlingSiteConfig {
    private List<Site> sites;

    @Data
    public static class Site {
        private String name;
        private String url;
    }
}
