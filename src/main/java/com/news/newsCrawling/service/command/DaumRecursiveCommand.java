package com.news.newsCrawling.service.command;

import com.news.newsCrawling.config.CrawlingSiteConfig;
import com.news.newsCrawling.config.CrawlingSiteConfig.Site;
import com.news.newsCrawling.model.contants.COMMAND_SITE_TYPE;
import com.news.newsCrawling.model.contants.DATA_SELECTOR;
import com.news.newsCrawling.model.vo.KeywordVo;
import com.news.newsCrawling.model.vo.MessageVo;
import com.news.newsCrawling.model.vo.NewsDataVo;
import com.news.newsCrawling.service.NewsCrawlingService;
import com.news.newsCrawling.util.KeyWordUtil;
import com.news.newsCrawling.util.RedisUtil;
import com.news.newsCrawling.util.SeleniumCrawlingUtil;
import com.news.newsCrawling.util.TextRankKeywordExtractor;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DaumRecursiveCommand implements CommandInterface {
    private final SeleniumCrawlingUtil seleniumCrawlingUtil;
    private final CrawlingSiteConfig crawlingSiteConfig;
    private final NewsCrawlingService newscrawlingService;
    private final KeyWordUtil keyWordUtil;
    private final TextRankKeywordExtractor textRankKeywordExtractor;
    private final RedisUtil redisUtil;

    // 사이드기사에 대해서는 재귀적 크롤링 x (메인기사에서만 재귀적 크롤링)
    @Override
    public void execute(MessageVo message) throws Exception {
    }
    // 사이드 기사 추출 및 저장
    @Override
    public void saveToDatabase(List<MessageVo> newsDataVo) throws IOException {

        List<NewsDataVo> savedList = new ArrayList<>();
        for (MessageVo message : newsDataVo) {
            // 사이드 기사(2번쨰 depth)만 처리
            if(message.getDepth() != 2) { continue;}
            Site daumSite = crawlingSiteConfig.getSites().get(COMMAND_SITE_TYPE.DAUM.getMessage());
            LinkedHashMap<String, String> dataSelectors = daumSite.getDataSelectors();
            try {
                // 기사의 메인 내용 추출
                WebElement webElement = seleniumCrawlingUtil.fetchHtml(message.getUrl()
                        , dataSelectors.get(DATA_SELECTOR.EMOTICON.getValue())
                        , dataSelectors.get(DATA_SELECTOR.WRAPPER.getValue()));

                NewsDataVo detailData = NewsDataVo.daumDataDetailFormat(webElement, dataSelectors);
                detailData.setUrl(message.getUrl());
                detailData.setSiteType(COMMAND_SITE_TYPE.DAUM);
                detailData.setDepth(2);
                savedList.add(detailData);
            } catch (Exception e) {
                System.err.println("Error processing URL: " + message.getUrl());
                e.printStackTrace();
            }
        }
        // DB 저장
        if(!savedList.isEmpty()) {
            newscrawlingService.saveAll(savedList);
        }

        List<String> corpus = redisUtil.getCorpus();
        List<KeywordVo> keywordVoList = new ArrayList<>();
        for (NewsDataVo data : savedList) {
            //TF-IDF 방식 키워드 추출
            List<String> keywords1 = keyWordUtil.extractKeywords(data.getContent(), corpus);

            //TEXTRANK 방식 키워드 추출
            List<String> keywords2 = textRankKeywordExtractor.extractKeywords(data.getContent());
            List<String> commonKeywords = new ArrayList<>(keywords1);
            commonKeywords.retainAll(keywords2);
            // 중복 단어 제거
            commonKeywords = new ArrayList<>(new HashSet<>(commonKeywords));

            KeywordVo keywordVo = KeywordVo.builder()
                    .id(data.getId())
                    .keyword(commonKeywords.toString())
                    .createAt(LocalDateTime.now())
                    .build();

            keywordVoList.add(keywordVo);
        }

        // 키워드 DB 저장
        newscrawlingService.insertKeywords(keywordVoList);
    }
}
