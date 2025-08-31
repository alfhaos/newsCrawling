package com.news.newsCrawling.util;

import com.news.newsCrawling.config.CrawlingInterface;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import static org.jsoup.Jsoup.connect;

@Component
public class JsoupCrawlingUtil implements CrawlingInterface {
    @Override
    public Document fetchHtml(String url) throws Exception {
        return connect(url).get();
    }
}
