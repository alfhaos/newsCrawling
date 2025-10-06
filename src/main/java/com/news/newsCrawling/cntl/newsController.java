package com.news.newsCrawling.cntl;

import com.news.newsCrawling.model.contants.COMMAND_SITE_TYPE;
import com.news.newsCrawling.model.vo.MessageVo;
import com.news.newsCrawling.model.vo.NewsDataVo;
import com.news.newsCrawling.service.NewsCrawlingService;
import com.news.newsCrawling.service.command.CommandFactory;
import com.news.newsCrawling.service.command.CommandInterface;
import com.news.newsCrawling.util.CommonResponse;
import com.news.newsCrawling.util.KeyWordUtil;
import com.news.newsCrawling.util.TextRankKeywordExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class newsController {
    private final NewsCrawlingService newscrawlingService;
    private final CommandFactory commandFactory;
    private final KeyWordUtil keyWordUtil;
    private final TextRankKeywordExtractor textRankKeywordExtractor;


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
}
