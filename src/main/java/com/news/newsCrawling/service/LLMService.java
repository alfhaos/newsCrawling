package com.news.newsCrawling.service;

import com.news.newsCrawling.model.common.OpenAIRequest;
import com.news.newsCrawling.model.common.OpenAIResponse;
import com.news.newsCrawling.model.common.TextSegmentDto;
import com.news.newsCrawling.model.vo.NewsDataVo;
import com.news.newsCrawling.util.VectorDatabaseUtil;
import dev.langchain4j.data.message.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.Map.Entry;

import static com.news.newsCrawling.model.common.OpenAIRequest.*;
import static org.springframework.ai.openai.api.OpenAiApi.ChatCompletion;
import static org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage.*;
import static org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest;

@Component
@RequiredArgsConstructor
public class LLMService {
    private final VectorDatabaseUtil vectorDatabaseUtil;
    private final OpenAiApi openAiApi;

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
    public HashMap<String, String> summarizeWeeklyNewsByKeywords(Map<String, List<NewsDataVo>> keywordNewsMap) {
        HashMap<String, String> keywordSummaries = new HashMap<>();

        for (String keyword : keywordNewsMap.keySet()) {
            // ✅ 1️⃣ 키워드 임베딩 가져오기
            float[] keywordEmbedding = vectorDatabaseUtil.getEmbeddingForKeyword(keyword);

            // ✅ 2️⃣ 벡터DB에서 해당 키워드와 유사한 뉴스 3개 검색
            List<NewsDataVo> relatedNewsList = vectorDatabaseUtil.searchSimilarNews(keywordEmbedding, 3);

            // ✅ 3️⃣ 뉴스 본문 텍스트 구성
            StringBuilder newsContextBuilder = new StringBuilder();
            for (int i = 0; i < relatedNewsList.size(); i++) {
                NewsDataVo news = relatedNewsList.get(i);
                String content = news.getContent();

                // 본문 길이 제한 (1,000자까지만)
//                if (content.length() > 1000) {
//                    content = content.substring(0, 1000) + "...";
//                }

                newsContextBuilder.append("- [")
                        .append(i + 1)
                        .append("] Content: ").append(content)
                        .append("\n\n");
            }

            // ✅ 4️⃣ 프롬프트 구성
            String prompt = """
            You are an AI that summarizes weekly news by topic.
            For the given keyword and related news articles:
            1. Summarize the main points.
            2. The summary should be within 3 sentences.
            3. Write in natural and concise Korean.
            4. Do not mention the news sources.
            """ + "\n\nKeyword: " + keyword + "\n\nNews Context:\n" + newsContextBuilder;

            ChatCompletionMessage systemMessage = new ChatCompletionMessage(
                    "You are a professional news summarizer.", Role.SYSTEM);
            ChatCompletionMessage userMessage = new ChatCompletionMessage(prompt, Role.USER);

            ChatCompletionRequest request = new ChatCompletionRequest(
                    List.of(systemMessage, userMessage),
                    "gpt-4o", // 모델
                    0.7,      // temperature
                    false
            );

            // ✅ 5️⃣ OpenAI API 호출
            ResponseEntity<ChatCompletion> response = openAiApi.chatCompletionEntity(request);

            // ✅ 6️⃣ 결과 저장
            String summary = response.getBody().choices().get(0).message().content();
            keywordSummaries.put(keyword, summary);
        }

        return keywordSummaries;
    }

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
//            ChatCompletionMessage systemMessage = new ChatCompletionMessage(
//                    systemPrompt, Role.SYSTEM);
//            ChatCompletionMessage userMessage = new ChatCompletionMessage("", Role.USER);

            // 💬 3️⃣ LLM 요청 (요약 생성)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apikey);
            Message userMessage = new Message(Role.USER.name().toLowerCase(), userPrompt);
            Message systemMessage = new Message(Role.SYSTEM.name().toLowerCase(), systemPrompt);
            OpenAIRequest request = new OpenAIRequest("gpt-4o", List.of(systemMessage,userMessage), 0.7);
            HttpEntity requestEntity = new HttpEntity<>(request, headers);
            ResponseEntity<OpenAIResponse> response = restTemplate.exchange(
                    baseUrl, HttpMethod.POST, requestEntity, OpenAIResponse.class
            );


//            ChatCompletionRequest request = new ChatCompletionRequest(
//                    List.of(systemMessage, userMessage),
//                    "gpt-3.5-turbo", // 모델
//                    0.7,      // temperature
//                    false
//            );
//            String response = openAiApi.chatCompletionEntity(request).toString();
            if (response == null) {
                throw new RuntimeException("Empty response from LLM summarization.");
            }

            String summary = response.getBody().getChoices().get(0).getMessage().getContent();
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
}
