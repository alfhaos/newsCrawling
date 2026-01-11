package com.news.newsCrawling.service;

import com.news.newsCrawling.model.common.OpenAIRequest;
import com.news.newsCrawling.model.common.OpenAIResponse;
import com.news.newsCrawling.model.common.TextSegmentDto;
import com.news.newsCrawling.model.vo.NewsDataVo;
import com.news.newsCrawling.util.VectorDatabaseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static com.news.newsCrawling.model.common.OpenAIRequest.Message;

@Component
@RequiredArgsConstructor
public class LLMService {
    private final VectorDatabaseUtil vectorDatabaseUtil;

    @Autowired
    private RestTemplate restTemplate;
    @Value("${spring.ai.openai.api-key}")
    private String apikey;
    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.text.options.model}")
    private String model;
    @Value("${spring.ai.openai.text.options.temperature}")
    private Double temperature;

    // 키워드 기반으로 뉴스 검색후 요약 해주는 함수
    // 요약본 생성 + 요약본 임베딩 생성 함수
    public NewsDataVo summarizeAndEmbed(NewsDataVo news, String keyword) {
        try {
            // 🧠 1️⃣ System prompt — 영어로 작성
            String systemPrompt = """
                    You are an AI assistant that summarizes news articles.
                    Your task is to read the given news article and summarize it accurately.
                    Focus on key people, events, causes, and implications.
                    Keep the summary clear and concise (about 1-3 sentences).
                    The summary should be written in **Korean**, even though instructions are in English.
                    emphasize and highlight the provided keyword.
                    Output only the summary text, without additional explanations.
                    
                    """;

            // 🗞 2️⃣ User prompt — 뉴스 제목 및 본문 삽입
            String userPrompt = """
                    Summarize the following news article in Korean based on the above instructions.

                    [Title] %s
                    [Ketword] %s
                    [Content]
                    %s
                    """.formatted(news.getTitle(),keyword, news.getContent());

            // 💬 3️⃣ LLM 요청 (요약 생성)
            String summary = sendLLMApi(List.of(
                    new Message("system", systemPrompt),
                    new Message("user", userPrompt)
            ));
            news.setSummaryContent(summary);
            TextSegmentDto textSegmentDto = NewsDataVo.convertToTextSegment(news, keyword);

            // 🔢 4️⃣ 요약문 임베딩 생성
            assert textSegmentDto != null;
            float[] summaryEmbeddingInput = vectorDatabaseUtil.ingestSegmentForDto(textSegmentDto);

            if (summaryEmbeddingInput == null) {
                throw new RuntimeException("Embedding generation failed.");
            }

            // 💾 5️⃣ 결과 저장 (요약문 + 임베딩)
            news.setSummaryContent(summary);
            news.setEmbedding(summaryEmbeddingInput);
            return news;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error during summarization or embedding: " + e.getMessage(), e);
        }
    }

    // 키워드 기반으로 사용자 쿼리 만드는 함수
    public String keywordReWriting(List<String> keywords) {
        // 🧠 1️⃣ System prompt — 영어로 작성
        String systemPrompt = """
                You are an assistant that converts keyword lists or short phrases into concise, natural Korean sentences\s
                suitable for embedding-based retrieval of news summaries.

                Your goal is to generate short declarative sentences (not questions) that describe the topic implied by the keywords.
                Avoid question forms like “무엇인가요?” or “어떻게 되었나요?”.\s
                Instead, write factual, summary-style statements that reflect news content.

                Respond only in Korean.
                    """;

        // 🗞 2️⃣ User prompt — 뉴스 제목 및 본문 삽입
        String userPrompt = """
                    Keywords: %s
                    """.formatted(keywords.toString());

        // 💬 3️⃣ LLM 요청 (요약 생성)
        return sendLLMApi(List.of(
                new Message("system", systemPrompt),
                new Message("user", userPrompt)
        ));
    }
    // 요약문 기반으로 주간/일간 뉴스데이토 요약 또는 트렌드 분석
    public String newsSummary(List<String> keywords, List<NewsDataVo> newsDataVoList) {
        // 🧠 1️⃣ System prompt — 영어로 작성
        String systemPrompt = """
                You are an expert news analyst AI that specializes in identifying trends and summarizing key insights from multiple related news articles. Your goal is to produce clear, concise, and insightful summaries and trend analyses based on the provided data.
               
                Respond only in Korean.
                    """;

        // 🗞 2️⃣ User prompt — 뉴스 제목 및 본문 삽입
        String userPrompt = """
                I have retrieved several news summaries that are semantically similar to a user query using embedding-based vector search. \s
                Below are the extracted summaries and relevant keywords. \s

                Please analyze them and generate a comprehensive **daily or weekly news summary and trend analysis**. \s
                Focus on identifying:
                - The main topics and recurring themes across the summaries \s
                - Notable events, developments, or shifts in public or industry attention \s
                - Emerging keywords or patterns that indicate new trends \s
                - A concise overall summary that captures the key insights \s
                Return a single concise paragraph (maximum 5 lines) that clearly captures the overall insights.

                Please analyze them and generate a **daily or weekly news summary and trend analysis** in **a single paragraph of no more than 5 lines**. \s
                Focus on identifying the key themes, notable events, and emerging trends while keeping the summary clear and concise.

                **Keywords:**
                %s

                **News Summaries:**
                %s
                    """.formatted(keywords.toString(), newsDataVoList.stream().map(NewsDataVo::getSummaryContent).toList());

        // 💬 3️⃣ LLM 요청 (요약 생성)
        return sendLLMApi(List.of(
                new Message("system", systemPrompt),
                new Message("user", userPrompt)
        ));
    }
    public String sendLLMApi(List<Message> messages) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apikey);
        OpenAIRequest request = new OpenAIRequest(model, messages, temperature);
        HttpEntity requestEntity = new HttpEntity<>(request, headers);
        ResponseEntity<OpenAIResponse> response = restTemplate.exchange(
                baseUrl, HttpMethod.POST, requestEntity, OpenAIResponse.class
        );

        return response.getBody().getChoices().get(0).getMessage().getContent();
    }
}
