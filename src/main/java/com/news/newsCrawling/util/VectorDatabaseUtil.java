package com.news.newsCrawling.util;

import com.news.newsCrawling.mapper.NewsCrawlingMapper;
import com.news.newsCrawling.model.common.TextSegmentDto;
import com.news.newsCrawling.model.vo.NewsDataVo;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Getter
public class VectorDatabaseUtil {
    private final OpenAiApi openAiApi;
    private final OpenAiEmbeddingModel embeddingModel;
    private final NewsCrawlingMapper mapper;
    /**
     * 분할된 텍스트 세그먼트를 임베딩하고 ChromaDB에 저장
     */
    public void ingestSegments(Set<TextSegmentDto> segmentList) {

        for (TextSegmentDto dto : segmentList) {
            // 임베딩 요청 생성
            EmbeddingRequest request = new EmbeddingRequest(
                    List.of(dto.getTextSegment().text()), // 텍스트 입력
                    null // 추가 옵션 (필요 시 설정)
            );

            // 임베딩 모델 호출
            EmbeddingResponse response = embeddingModel.embedForResponse(request.getInstructions());

            // 응답에서 임베딩 벡터 추출
            List<Embedding> embeddingVector = response.getResults();
            Embedding embedding = embeddingVector.get(0);
            // 데이터베이스에 텍스트와 임베딩 벡터 저장

            mapper.updateEmbeddingData(dto.getId(), embedding.getOutput());
        }

    }

    public float[] ingestSegmentForDto(TextSegmentDto dto) {

        // 임베딩 요청 생성
        EmbeddingRequest request = new EmbeddingRequest(
                List.of(dto.getTextSegment().text()), // 텍스트 입력
                null // 추가 옵션 (필요 시 설정)
        );

        // 임베딩 모델 호출
        EmbeddingResponse response = embeddingModel.embedForResponse(request.getInstructions());

        // 응답에서 임베딩 벡터 추출
        List<Embedding> embeddingVector = response.getResults();
        Embedding embedding = embeddingVector.get(0);

        return embedding.getOutput();

    }

    public float[] getEmbeddingForKeyword(String keyword) {

        // 2️⃣ 임베딩 생성
        EmbeddingRequest request = new EmbeddingRequest(
                List.of(keyword), // 키워드 입력
                null // 추가 옵션 (필요 시 설정)
        );

        // OpenAI 임베딩 생성
        EmbeddingResponse response = embeddingModel.embedForResponse(request.getInstructions());
        float[] generatedEmbedding = response.getResults().get(0).getOutput();

        return generatedEmbedding;
    }
    public List<NewsDataVo> searchSimilarNews(float[] embedding, int topN, String searchDate) {
        return mapper.searchSimilarNews(embedding, topN, searchDate);
    }
}
