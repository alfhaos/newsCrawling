package com.news.newsCrawling.service;

import com.news.newsCrawling.mapper.NewsCrawlingMapper;
import com.news.newsCrawling.model.common.OpenAIRequest;
import com.news.newsCrawling.model.common.OpenAIResponse;
import com.news.newsCrawling.model.vo.NewsDataVo;
import org.junit.jupiter.api.Test;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.ai.openai.api.OpenAiApi.*;
import static org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage.*;

@SpringBootTest
@ActiveProfiles("local") // 테스트용 프로파일 활성화
class LLMServiceTest {

    @Autowired
    private NewsCrawlingMapper mapper;

    @Autowired
    private LLMService llmService;
    @Autowired
    private NewsCrawlingService newsCrawlingService;

    @Autowired
    private OpenAiApi openAiApi;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${spring.ai.openai.api-key}")
    private String apikey;
    @Test
    @Rollback(value = true)
    @Transactional
    public void testLLM() throws Exception {

        NewsDataVo data = mapper.selectContentById(357L);
        String keywords = "경제, 대통령";

        NewsDataVo temp = llmService.summarizeAndEmbed(data, keywords);
        System.out.println(temp);
    }

    @Test
    @Rollback(value = true)
    @Transactional
    public void testLLM2() throws Exception {

        ChatCompletionMessage chatCompletionMessage = new ChatCompletionMessage("Hello world", Role.USER);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apikey);
        OpenAIRequest.Message message = new OpenAIRequest.Message("user", "안녕하세요");
        OpenAIRequest request = new OpenAIRequest("gpt-4o", Collections.singletonList(message), 0.7);
        HttpEntity requestEntity = new HttpEntity<>(request, headers);
        ResponseEntity<OpenAIResponse> response = restTemplate.exchange(
                "https://api.openai.com/v1/chat/completions", HttpMethod.POST, requestEntity, OpenAIResponse.class
        );

        System.out.println(response.getBody().getChoices().get(0).getMessage().getContent());
        assertThat(response).isNotNull();
        assertThat(response.getBody()).isNotNull();
    }
    @Test
    @Rollback(value = true)
    @Transactional
    public void testLLMKeywordReWriting() throws Exception {

        List<String> keywords = newsCrawlingService.weeklyKeyword();
        String response = llmService.keywordReWriting(keywords);
        assertThat(response).isNotNull();
        System.out.println(response);
    }
}