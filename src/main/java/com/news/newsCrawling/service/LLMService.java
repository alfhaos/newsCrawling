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
//    public HashMap<String, String> summarizeWeeklyNewsByKeywords(Map<String, List<NewsDataVo>> keywordNewsMap) {
//        HashMap<String, String> keywordSummaries = new HashMap<>();
//
//        for (String keyword : keywordNewsMap.keySet()) {
//            // ✅ 1️⃣ 키워드 임베딩 가져오기
//            float[] keywordEmbedding = vectorDatabaseUtil.getEmbeddingForKeyword(keyword);
//
//            // ✅ 2️⃣ 벡터DB에서 해당 키워드와 유사한 뉴스 3개 검색
//            List<NewsDataVo> relatedNewsList = vectorDatabaseUtil.searchSimilarNews(keywordEmbedding, 3);
//
//            // ✅ 3️⃣ 뉴스 본문 텍스트 구성
//            StringBuilder newsContextBuilder = new StringBuilder();
//            for (int i = 0; i < relatedNewsList.size(); i++) {
//                NewsDataVo news = relatedNewsList.get(i);
//                String content = news.getContent();
//
//                // 본문 길이 제한 (1,000자까지만)
////                if (content.length() > 1000) {
////                    content = content.substring(0, 1000) + "...";
////                }
//
//                newsContextBuilder.append("- [")
//                        .append(i + 1)
//                        .append("] Content: ").append(content)
//                        .append("\n\n");
//            }
//
//            // ✅ 4️⃣ 프롬프트 구성
//            String prompt = """
//            You are an AI that summarizes weekly news by topic.
//            For the given keyword and related news articles:
//            1. Summarize the main points.
//            2. The summary should be within 3 sentences.
//            3. Write in natural and concise Korean.
//            4. Do not mention the news sources.
//            """ + "\n\nKeyword: " + keyword + "\n\nNews Context:\n" + newsContextBuilder;
//
//            ChatCompletionMessage systemMessage = new ChatCompletionMessage(
//                    "You are a professional news summarizer.", Role.SYSTEM);
//            ChatCompletionMessage userMessage = new ChatCompletionMessage(prompt, Role.USER);
//
//            ChatCompletionRequest request = new ChatCompletionRequest(
//                    List.of(systemMessage, userMessage),
//                    "gpt-4o", // 모델
//                    0.7,      // temperature
//                    false
//            );
//
//            // ✅ 5️⃣ OpenAI API 호출
//            ResponseEntity<ChatCompletion> response = openAiApi.chatCompletionEntity(request);
//
//            // ✅ 6️⃣ 결과 저장
//            String summary = response.getBody().choices().get(0).message().content();
//            keywordSummaries.put(keyword, summary);
//        }
//
//        return keywordSummaries;
//    }

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
