package com.news.newsCrawling.service;

import com.news.newsCrawling.util.VectorDatabaseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LLMService {
    private final VectorDatabaseUtil vectorDatabaseUtil;

    public String queryDatabaseAndGenerateResponse(String userQuery) {
        // 1️⃣ 사용자 질문 임베딩 생성
//        Embedding queryEmbedding = vectorDatabaseUtil.getEmbeddingModel().embed(userQuery).content();

        // 2️⃣ Chroma에서 관련 문서 검색
//        List<EmbeddingMatch> matches = vectorDatabaseUtil.getEmbeddingStore().findRelevant(queryEmbedding, 5);

        // 3️⃣ 문맥(context) 구성
//        String context = matches.stream()
//                .map(match -> match.embedded().toString()) // ✅ text() → embedded()
//                .collect(Collectors.joining("\n\n"));
//
//        // 4️⃣ LLM 프롬프트 구성
//        // 4️⃣ 프롬프트 구성
//        String prompt = """
//                아래는 관련된 뉴스 기사입니다.
//                1.사용자의 질문에 답변하세요.
//                2.한국어로 자연스럽고 간결하게 작성해주세요.
//                3.주간 뉴스 요약본을 작성하는 느낌으로 작성해주세요.
//                4.키워드에 맞는 뉴스 내용을 중심으로 작성해주세요.
//                5.출처는 언급하지 마세요.
//                뉴스 문맥:
//                %s
//
//                사용자 질문:
//                %s
//                """.formatted(context, userQuery);
//
//        // 5️⃣ 요청 파라미터 설정
//        Parameters parameters = Parameters.builder()
//                .temperature(temperature)
//                .maxNewTokens(maxTokens)
//                .build();
//
//        TextGenerationRequest request = TextGenerationRequest.builder()
//                .inputs(prompt)
//                .parameters(parameters)
//                .build();
//
//        // 6️⃣ Hugging Face 모델 호출
//        TextGenerationResponse response = huggingFaceClient.generate(request);
//
//        // 7️⃣ 결과 반환
//        if (response.generatedText() != null && !response.generatedText().isEmpty()) {
//            return response.generatedText();
//        } else {
//            return "모델로부터 응답을 받지 못했습니다.";
//        }
        return "";
    }
}
