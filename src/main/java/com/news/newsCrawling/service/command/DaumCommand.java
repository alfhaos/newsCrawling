package com.news.newsCrawling.service.command;

import com.news.newsCrawling.config.CrawlingSiteConfig;
import com.news.newsCrawling.config.CrawlingSiteConfig.Site;
import com.news.newsCrawling.model.common.TextSegmentDto;
import com.news.newsCrawling.model.contants.AGENT_ROLE;
import com.news.newsCrawling.model.contants.COMMAND_SITE_TYPE;
import com.news.newsCrawling.model.contants.DATA_SELECTOR;
import com.news.newsCrawling.model.vo.KeywordVo;
import com.news.newsCrawling.model.vo.MessageVo;
import com.news.newsCrawling.model.vo.NewsDataVo;
import com.news.newsCrawling.service.LLMService;
import com.news.newsCrawling.service.NewsCrawlingService;
import com.news.newsCrawling.util.*;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DaumCommand implements CommandInterface {
    private final SeleniumCrawlingUtil seleniumCrawlingUtil;
    private final CrawlingSiteConfig crawlingSiteConfig;
    private final RedisUtil redisUtil;
    private final NewsCrawlingService newscrawlingService;
    private final KafkaTemplate<String, MessageVo> kafkaTemplate;
    private final KeyWordUtil keyWordUtil;
    private final TextRankKeywordExtractor textRankKeywordExtractor;
    private final VectorDatabaseUtil vectorDatabaseUtil;
    private final LLMService llmService;
    @Value("${agent.topic}")
    private String topic;

    @Value("${agent.role}")
    private String agentRole;

    // 메인 사이트 최초 추출
    @Override
    public void execute(MessageVo message) throws Exception {


        // 워커가 아닐경우 실행하지 않음
        if (!AGENT_ROLE.MAIN.name().equalsIgnoreCase(agentRole)) {return;}
        
        // 키워드 분석을 위한 코퍼스 생성
        List<String> corpus = redisUtil.getCorpus();

        if(corpus == null || corpus.isEmpty()) {
            redisUtil.saveCorpusData(newscrawlingService.makeCorpus(null, 10000));
        }
        // 메인 기사(depth 1)만 처리
        if(message.getDepth() != 1) { return;}

        Site daumSite = crawlingSiteConfig.getSites().get(COMMAND_SITE_TYPE.DAUM.getMessage());
        LinkedHashMap<String, String> selectors = daumSite.getSelectors();

        // 셀레니움을 사용하여 메인 페이지의 요소 가져오기
        List<WebElement> depth1Elements = seleniumCrawlingUtil.fetchMainHtml(message.getUrl(), selectors.get(DATA_SELECTOR.DEPTH1.getValue()));

        if (depth1Elements.isEmpty()) {
            System.out.println("DaumCommand - No elements found for depth1 selector.");
            return;
        }

        WebElement depth1Element = depth1Elements.get(0);
        List<WebElement> depth2Elements = depth1Element.findElements(By.cssSelector(selectors.get(DATA_SELECTOR.DEPTH2.getValue())));

        List<MessageVo> list = MessageVo.dataFormatForMessage(depth2Elements, COMMAND_SITE_TYPE.DAUM);
        for (MessageVo messageVo : list) {
            if (redisUtil.getData(messageVo.getUrl()) == null) {
                // Redis에 저장
                redisUtil.saveData(messageVo.getUrl(), "");
                System.out.println("DaumCommand - New URL Found: " + messageVo.getUrl());
            } else {
                // 이미 처리된 URL인 경우 리스트에서 제거
                list.remove(messageVo);
                System.out.println("DaumCommand - URL Already Processed: " + messageVo.getUrl());
            }
        }

        // TODO: 카프카 메세지 전송 후 워커에서 DB에 저장해야 하지만 일단 바로 저장
        for (MessageVo messageVo : list) {
            kafkaTemplate.send(topic, messageVo);
        }

//        saveToDatabase(list);
    }

    @Override
    public void saveToDatabase(List<MessageVo> messageList) throws IOException {
        List<NewsDataVo> savedList = new ArrayList<>();

        if(messageList.isEmpty()) { return; }
        Site daumSite = crawlingSiteConfig.getSites().get(COMMAND_SITE_TYPE.DAUM.getMessage());
        LinkedHashMap<String, String> dataSelectors = daumSite.getDataSelectors();
        LinkedHashMap<String, String> rcDataSelectors = daumSite.getRecursiveDataSelectors();
        // 각 url 접속 후 내용 추출
        for (MessageVo dataVo : messageList) {
            try {
                // 기사의 메인 내용 추출
                WebElement webElement = seleniumCrawlingUtil.fetchHtml(dataVo.getUrl(), dataSelectors.get(DATA_SELECTOR.EMOTICON.getValue()), dataSelectors.get(DATA_SELECTOR.WRAPPER.getValue()));
                NewsDataVo detailData = NewsDataVo.daumDataDetailFormat(webElement, dataSelectors);
                detailData.setUrl(dataVo.getUrl());
                detailData.setSiteType(COMMAND_SITE_TYPE.DAUM);
                detailData.setDepth(1);
                savedList.add(detailData);


                // 재귀적 크롤링을 위한 사이드 영역의 기사 추출 및 카프카를 통해 워커에게 전송
                WebElement sideElement = seleniumCrawlingUtil.fetchHtml(dataVo.getUrl(), rcDataSelectors.get(DATA_SELECTOR.WRAPPER.getValue()));
                // li 태그 목록 가져오기
                List<WebElement> listItems = sideElement.findElements(By.tagName("li"));
                List<MessageVo> urlList = new ArrayList<>();
                // 각 li 태그의 a 태그에서 href 추출
                for (WebElement listItem : listItems) {
                    try {
                        WebElement anchor = listItem.findElement(By.cssSelector(rcDataSelectors.get(DATA_SELECTOR.URL.getValue())));
                        String href = anchor.getAttribute("href");
                        // 중복 체크 후 리스트에 추가
                        if (redisUtil.getData(href) == null) {
                            urlList.add(MessageVo.builder()
                                    .url(href)
                                    .depth(2)
                                    .siteType(COMMAND_SITE_TYPE.DAUM)
                                    .build());

                            redisUtil.saveData(href, "");
                        }
                    } catch (NoSuchElementException e) {
                        // 광고등 중간에 이상이 있을경우 무시하고 진행
                    }
                }

                //사이드 기사 카프카에 재귀적 크롤링 메세지 전송
                for (MessageVo sideNews : urlList) {
                    kafkaTemplate.send(topic, sideNews);
                }

            } catch (Exception e) {
                System.err.println("Error processing URL: " + dataVo.getUrl());
                e.printStackTrace();
            }
        }

        // DB 저장
        if(!savedList.isEmpty()){
            newscrawlingService.saveAll(savedList);
        }

        List<String> corpus = redisUtil.getCorpus();
        List<KeywordVo> keywordVoList = new ArrayList<>();
        for (NewsDataVo newsDataVo : savedList) {
            //TF-IDF 방식 키워드 추출
            List<String> keywords1 = keyWordUtil.extractKeywords(newsDataVo.getContent(), corpus);

            //TEXTRANK 방식 키워드 추출
            List<String> keywords2 = textRankKeywordExtractor.extractKeywords(newsDataVo.getContent());
            List<String> commonKeywords = new ArrayList<>(keywords1);
            commonKeywords.retainAll(keywords2);
            // 중복 단어 제거
            commonKeywords = new ArrayList<>(new HashSet<>(commonKeywords));

            KeywordVo keywordVo = KeywordVo.builder()
                    .id(newsDataVo.getId())
                    .keyword(commonKeywords.toString())
                    .createAt(LocalDateTime.now())
                    .build();

            keywordVoList.add(keywordVo);
            NewsDataVo result = llmService.summarizeAndEmbed(newsDataVo, commonKeywords.isEmpty() ? "" : commonKeywords.get(0));

            // 요약문 + 임베딩 DB 저장
            newscrawlingService.updateSummaryAndEmbedding(result);
        }

        // 키워드 DB 저장
        newscrawlingService.insertKeywords(keywordVoList);
    }
}
