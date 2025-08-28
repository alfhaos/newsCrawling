package com.news.newscrawling.cntl;

import com.news.newscrawling.config.CrawlingSiteConfig;
import com.news.newscrawling.config.CrawlingSiteConfig.Site;
import com.news.newscrawling.util.CommonResponse;
import com.news.newscrawling.util.JsoupCrawlingUtil;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class newsController {
    private final JsoupCrawlingUtil jsoupCrawlingUtil;
    private final CrawlingSiteConfig crawlingSiteConfig;


    @GetMapping("/")
    private CommonResponse<Object> test() throws Exception {

        List<Site> sites = crawlingSiteConfig.getSites();
        Document doc = null;
        for (Site site : sites) {
            doc = jsoupCrawlingUtil.fetchHtml(site.getUrl());
        }
        return new CommonResponse<>(doc.toString());
    }
}
