package com.news.newsCrawling.util;

import dev.langchain4j.data.segment.TextSegment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

@Component
@RequiredArgsConstructor
@Getter
public class VectorDatabaseUtil {
    private final OpenAiApi openAiApi;
    private final DataSource pgVectorDataSource;
    private final OpenAiEmbeddingModel embeddingModel;
    /**
     * 분할된 텍스트 세그먼트를 임베딩하고 ChromaDB에 저장
     */
    public void ingestSegments(List<TextSegment> segments) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(pgVectorDataSource);

        // 데이터베이스에 저장할 쿼리
        String insertQuery = "INSERT INTO embeddings (text, embedding) VALUES (?, ?)";

        for (TextSegment segment : segments) {
            // 임베딩 요청 생성
            EmbeddingRequest request = new EmbeddingRequest(
                    List.of(segment.text()), // 텍스트 입력
                    null // 추가 옵션 (필요 시 설정)
            );

            // 임베딩 모델 호출
            EmbeddingResponse response = embeddingModel.embedForResponse(request.getInstructions());

            // 응답에서 임베딩 벡터 추출
            List<Embedding> embeddingVector = response.getResults();
            Embedding embedding = embeddingVector.get(0);
            // 데이터베이스에 텍스트와 임베딩 벡터 저장
            jdbcTemplate.update(insertQuery, segment.text(),embedding.getOutput());
        }
    }

}
