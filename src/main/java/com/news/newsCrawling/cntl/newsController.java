package com.news.newsCrawling.cntl;

import com.news.newsCrawling.model.common.SearchDto;
import com.news.newsCrawling.model.contants.COMMAND_SITE_TYPE;
import com.news.newsCrawling.model.contants.SEARCH_DATE;
import com.news.newsCrawling.model.contants.SEARCH_TYPE;
import com.news.newsCrawling.model.vo.MessageVo;
import com.news.newsCrawling.model.vo.NewsDataVo;
import com.news.newsCrawling.service.EmailService;
import com.news.newsCrawling.service.NewsCrawlingService;
import com.news.newsCrawling.service.command.CommandFactory;
import com.news.newsCrawling.service.command.CommandInterface;
import com.news.newsCrawling.util.CommonResponse;
import com.news.newsCrawling.util.KeyWordUtil;
import com.news.newsCrawling.util.TextRankKeywordExtractor;
import com.news.newsCrawling.util.VectorDatabaseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class newsController {
    private final NewsCrawlingService newscrawlingService;
    private final CommandFactory commandFactory;
    private final KeyWordUtil keyWordUtil;
    private final TextRankKeywordExtractor textRankKeywordExtractor;
    private final EmailService emailService;
    private final VectorDatabaseUtil vectorDatabaseUtil;
    @GetMapping("/")
    private CommonResponse<Object> test() throws Exception {

        int test = newscrawlingService.test();
        CommandInterface command = commandFactory.getCommand(COMMAND_SITE_TYPE.DAUM.getValue());
        MessageVo messageVo = MessageVo.builder()
                .url("https://news.daum.net/")
                .siteType(COMMAND_SITE_TYPE.DAUM)
                .depth(1)
                .build();

        command.execute(messageVo);

        return new CommonResponse<>(null);
    }

    @PostMapping("/makeCorpus")
    private CommonResponse<Object> makeCorpus(@RequestParam Long id) throws Exception {

        List<String> corpusList = keyWordUtil.extractCorpus(newscrawlingService
                .makeCorpus(LocalDateTime.of(2025, 9, 21, 0, 0), 10000));

        NewsDataVo newsDataVo = newscrawlingService.selectContentById(id);

        List<String> keywords = keyWordUtil.extractKeywords(newsDataVo.getContent(), corpusList);
        List<String> keywords2 = textRankKeywordExtractor.extractKeywords(newsDataVo.getContent());

        List<String> commonKeywords = new ArrayList<>(keywords);
        commonKeywords.retainAll(keywords2);
        // 중복 단어 제거
        commonKeywords = new ArrayList<>(new HashSet<>(commonKeywords));
        
        return new CommonResponse<>(commonKeywords);
    }

    @GetMapping("/search")
    private CommonResponse<Object> searchNewsData(
            @RequestBody SearchDto searchDto) throws Exception {
        List<NewsDataVo> searchResults = newscrawlingService.searchByKeyword(searchDto);
        return new CommonResponse<>(searchResults);
    }

    @GetMapping("/daily/keyword")
    private CommonResponse<Object> dailyKeyword() throws Exception {
        List<String> keywords = newscrawlingService.dailyKeyword();
        return new CommonResponse<>(keywords);
    }

    @GetMapping("/test/mail")
    private CommonResponse<Object> mailTest(@RequestBody SearchDto searchDto) throws Exception {

        searchDto.setSearchType(SEARCH_TYPE.EMOTICON);
        searchDto.setSearchDate(SEARCH_DATE.DAILY);
        // 사용자가 반응한 이모티콘 갯수 랭크 기반 검색 결과 추출
        List<NewsDataVo> popularList = newscrawlingService.searchByPopular(searchDto);

        // 키워드 기반 검색 결과 추출
        List<String> keywords = newscrawlingService.dailyKeyword();
        searchDto.setSearchType(SEARCH_TYPE.KEYWORD);
        Map<String, List<NewsDataVo>> keywordList = new HashMap<>();
        for (String keyword : keywords) {
            searchDto.setKeyword(keyword);
            List<NewsDataVo> searchResults = newscrawlingService.searchByKeyword(searchDto);
            keywordList.put(keyword, searchResults);
        }

        emailService.sendEmail(keywordList, popularList, SEARCH_DATE.DAILY);
        return new CommonResponse<>(keywordList);
    }

    @GetMapping("/test/mail/weekly")
    private CommonResponse<Object> mailTestWeekly(@RequestBody SearchDto searchDto) throws Exception {

        searchDto.setSearchType(SEARCH_TYPE.EMOTICON);
        searchDto.setSearchDate(SEARCH_DATE.WEEKLY);
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
        for (String s : keywordList.keySet()) {
            List<NewsDataVo> newsDataVos = keywordList.get(s);
            vectorDatabaseUtil.ingestSegments(NewsDataVo.convertToTextSegments(newsDataVos, s));
        }
//        emailService.sendEmail(keywordList, popularList, SEARCH_DATE.WEEKLY);
        return new CommonResponse<>(keywordList);
    }
}
