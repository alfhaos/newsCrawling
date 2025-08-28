package com.news.newscrawling.config;

import org.jsoup.nodes.Document;

public interface CrawlingInterface {
    Document fetchHtml(String url) throws Exception;
}
