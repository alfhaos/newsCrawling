package com.news.newsCrawling.cntl;

import com.news.newsCrawling.config.CrawlingSiteConfig;
import com.news.newsCrawling.model.contants.COMMAND_SITE_TYPE;
import com.news.newsCrawling.service.NewscrawlingService;
import com.news.newsCrawling.util.JsoupCrawlingUtil;
import com.news.newsCrawling.util.RedisUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
public class newsControllerTest {

    @Autowired
    private JsoupCrawlingUtil jsoupCrawlingUtil;
    @Autowired
    private CrawlingSiteConfig crawlingSiteConfig;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private NewscrawlingService newscrawlingService;

    @Test
    @Rollback(value = true)
    @Transactional
    public void testExtractData() throws Exception {
        String url = "https://v.daum.net/v/20250831190224453";
        CrawlingSiteConfig.Site daumSite = crawlingSiteConfig.getSites().get(COMMAND_SITE_TYPE.DAUM.getMessage());
        LinkedHashMap<String, String> dataSelectors = daumSite.getDataSelectors();
        Document doc = jsoupCrawlingUtil.fetchHtml(url);
        Element wrapper = doc.selectFirst(dataSelectors.get("wrapper"));

        Element commentCnt = wrapper.selectFirst(dataSelectors.get("createAt"));
        Elements emotionCnt = wrapper.select(dataSelectors.get("emotionCnt"));

        System.out.println(Integer.parseInt(emotionCnt.get(0).text()));
        System.out.println(Integer.parseInt(emotionCnt.get(1).text()));
        System.out.println(Integer.parseInt(emotionCnt.get(2).text()));
        System.out.println(Integer.parseInt(emotionCnt.get(3).text()));
        System.out.println(Integer.parseInt(emotionCnt.get(4).text()));
        assertNotNull(commentCnt);
    }
}