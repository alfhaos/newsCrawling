package com.news.newsCrawling.service.command;

import com.news.newsCrawling.config.CrawlingSiteConfig;
import com.news.newsCrawling.config.CrawlingSiteConfig.Site;
import com.news.newsCrawling.model.contants.COMMAND_SITE_TYPE;
import com.news.newsCrawling.model.vo.MessageVo;
import com.news.newsCrawling.model.vo.NewsDataVo;
import com.news.newsCrawling.service.NewsCrawlingService;
import com.news.newsCrawling.util.RedisUtil;
import com.news.newsCrawling.util.SeleniumCrawlingUtil;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DaumRecursiveCommand implements CommandInterface {
    private final SeleniumCrawlingUtil seleniumCrawlingUtil;
    private final CrawlingSiteConfig crawlingSiteConfig;
    private final RedisUtil redisUtil;
    private final NewsCrawlingService newscrawlingService;


    // 사이드 기사 추출 및 저장
    @Override
    public void execute(MessageVo message) throws Exception {

        // 사이드 기사(2번쨰 depth)만 처리
        if(message.getDepth() != 2) { return;}

        List<NewsDataVo> savedList = new ArrayList<>();

        Site daumSite = crawlingSiteConfig.getSites().get(COMMAND_SITE_TYPE.DAUM.getMessage());
        LinkedHashMap<String, String> dataSelectors = daumSite.getDataSelectors();
        try {
            // 기사의 메인 내용 추출
            WebElement webElement = seleniumCrawlingUtil.fetchHtml(message.getUrl(), dataSelectors.get("emoticon"), dataSelectors.get("wrapper"));
            NewsDataVo detailData = NewsDataVo.daumDataDetailFormat(webElement, dataSelectors);
            detailData.setUrl(message.getUrl());
            detailData.setSiteType(COMMAND_SITE_TYPE.DAUM);
            savedList.add(detailData);
        } catch (Exception e) {
            System.err.println("Error processing URL: " + message.getUrl());
            e.printStackTrace();
        }
        // DB 저장
        newscrawlingService.saveAll(savedList);
    }

    @Override
    public void saveToDatabase(List<MessageVo> newsDataVo) throws IOException {
    }
}
