package com.news.newsCrawling.config;

import org.jsoup.nodes.Document;

public interface CrawlingInterface {
    Document fetchHtml(String url) throws Exception;
}
