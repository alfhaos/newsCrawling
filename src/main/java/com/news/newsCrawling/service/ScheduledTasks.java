package com.news.newsCrawling.service;

import com.news.newsCrawling.model.common.SearchDto;
import com.news.newsCrawling.model.contants.AGENT_ROLE;
import com.news.newsCrawling.model.contants.COMMAND_SITE_TYPE;
import com.news.newsCrawling.model.contants.SEARCH_DATE;
import com.news.newsCrawling.model.contants.SEARCH_TYPE;
import com.news.newsCrawling.model.vo.MessageVo;
import com.news.newsCrawling.model.vo.NewsDataVo;
import com.news.newsCrawling.service.command.CommandFactory;
import com.news.newsCrawling.service.command.CommandInterface;
import com.news.newsCrawling.util.VectorDatabaseUtil;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final NewsCrawlingService newscrawlingService;
    private final CommandFactory commandFactory;
    private final EmailService emailService;
    private final VectorDatabaseUtil vectorDatabaseUtil;
    private final LLMService llmService;

    @Value("${crawling.sites.daum.url}")
    private String daumUrl;

    @Value("${agent.role}")
    private String agentRole;

    private static final int topN = 10;

    // 매일 오전 9시에 뉴스 크롤링 작업 실행
    @Scheduled(cron = "0 49 19 * * ?")
    public void crawlingDailyWithNewsData() throws Exception {
        if(agentRole.equals(AGENT_ROLE.WORKER.name())) {
            return;
        }
        CommandInterface command = commandFactory.getCommand(COMMAND_SITE_TYPE.DAUM.getValue());
        MessageVo messageVo = MessageVo.builder()
                .url(daumUrl)
                .siteType(COMMAND_SITE_TYPE.DAUM)
                .depth(1)
                .build();

        command.execute(messageVo);
    }

    // 매일 오전 12시에 이메일 발송 작업 실행
    @Scheduled(cron = "0 10 15 * * ?")
    public void sendEmailDaily() throws MessagingException {
        if(agentRole.equals(AGENT_ROLE.WORKER.name())) {
            return;
        }
        sendEamil(SEARCH_DATE.DAILY);
    }

    // 매주 월요일 오전 9시에 실행
    @Scheduled(cron = "0 0 9 ? * MON")
    public void sendEmailWeekly() throws MessagingException {
        if(agentRole.equals(AGENT_ROLE.WORKER.name())) {
            return;
        }
        sendEamil(SEARCH_DATE.WEEKLY);
    }

    private void sendEamil(SEARCH_DATE searchDate) throws MessagingException {
        SearchDto searchDto = new SearchDto();

        searchDto.setSearchType(SEARCH_TYPE.EMOTICON);
        searchDto.setSearchDate(searchDate);
        // 사용자가 반응한 이모티콘 갯수 랭크 기반 검색 결과 추출
        List<NewsDataVo> popularList = newscrawlingService.searchByPopular(searchDto);

        // 키워드 기반 검색 결과 추출
        List<String> keywords = newscrawlingService.weeklyKeyword();
        searchDto.setSearchType(SEARCH_TYPE.KEYWORD);
        Map<String, List<NewsDataVo>> keywordList = new HashMap<>();
        for (String keyword : keywords) {
            searchDto.setKeyword(keyword);
            List<NewsDataVo> searchResults = newscrawlingService.searchByKeyword(searchDto);
            keywordList.put(keyword, searchResults);
        }

        String llmResult = llmService.keywordReWriting(keywords);
        float[] llmEmbedding = vectorDatabaseUtil.getEmbeddingForKeyword(llmResult);
        List<NewsDataVo> refinedKeywords = vectorDatabaseUtil.searchSimilarNews(llmEmbedding, topN, searchDate.name());
        String llmSummaryResult = llmService.newsSummary(keywords, refinedKeywords);
//        HashMap<String, String> summarizeWeeklyNewsByKeywords = llmService.summarizeWeeklyNewsByKeywords(keywordList);

        emailService.sendEmail(keywordList, popularList, searchDate, llmSummaryResult);
    }
}
