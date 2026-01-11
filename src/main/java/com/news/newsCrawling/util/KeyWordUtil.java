package com.news.newsCrawling.util;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.util.BytesRef;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KeyWordUtil {
    // 추출할 키워드 수
    private static final int topN = 5;
    private static final String fieldName = "content";
    public List<String> extractCorpus(List<String> rawContents) {

        // 전처리 및 코퍼스 생성
        List<String> corpus = new ArrayList<>();
        for (String content : rawContents) {
            if (content != null && !content.isEmpty()) {
                // 전처리: HTML 태그 제거, 소문자 변환, 불필요한 기호 제거
                String processedContent = content.replaceAll("<[^>]*>", "") // HTML 태그 제거
                        .replaceAll("[^\\p{L}\\p{Nd}\\s]", "") // 특수문자 제거
                        .toLowerCase(); // 소문자 변환
                corpus.add(processedContent);
            }
        }
        return corpus;
    }

    public List<String> extractKeywords(String document, List<String> corpus) throws IOException {
        // 메모리 인덱스 생성
        try (ByteBuffersDirectory ramDirectory = new ByteBuffersDirectory();
             StandardAnalyzer analyzer = new StandardAnalyzer()) {

            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            try (IndexWriter writer = new IndexWriter(ramDirectory, config)) {
                FieldType fieldType = new FieldType(TextField.TYPE_STORED);
                fieldType.setStoreTermVectors(true); // TermVector 저장 활성화
                fieldType.setStoreTermVectorPositions(true);
                fieldType.setStoreTermVectorOffsets(true);

                // 코퍼스 문서 추가
                for (String doc : corpus) {
                    Document luceneDoc = new Document();
                    luceneDoc.add(new Field(fieldName, doc, fieldType));
                    writer.addDocument(luceneDoc);
                }

                // 분석 대상 문서 추가
                Document targetDoc = new Document();
                targetDoc.add(new Field(fieldName, document, fieldType));
                writer.addDocument(targetDoc);

                writer.commit(); // 명시적으로 커밋
            }

            // 인덱스 리더 생성
            try (IndexReader reader = DirectoryReader.open(ramDirectory)) {
                if (reader.maxDoc() == 0) {
                    throw new IllegalStateException("인덱스에 문서가 없습니다.");
                }

                ClassicSimilarity similarity = new ClassicSimilarity();
                Map<String, Float> tfidfScores = new HashMap<>();

                // 마지막 문서의 TermVector 가져오기 (document가 마지막에 추가됨)
                Terms terms = reader.getTermVector(reader.maxDoc() - 1, fieldName);
                if (terms != null) {
                    TermsEnum termsEnum = terms.iterator();
                    BytesRef term;
                    while ((term = termsEnum.next()) != null) {
                        String termText = term.utf8ToString();
                        if (termText.length() >= 2) { // 2글자 이상 필터링
                            float tf = similarity.tf(termsEnum.totalTermFreq());
                            float idf = similarity.idf(termsEnum.docFreq(), reader.numDocs());
                            tfidfScores.put(termText, tf * idf);
                        }
                    }
                }
                // TF-IDF 점수 기준 상위 N개 키워드 추출
                List<String> result = new ArrayList<>(tfidfScores.entrySet().stream()
                        .sorted((e1, e2) -> Float.compare(e2.getValue(), e1.getValue()))
                        .limit(topN)
                        .map(Map.Entry::getKey)
                        .toList());

                return KoreanPostpositionRemover.removePostposition(result).stream()
                        .filter(word -> word.length() >= 2)
                        .toList();
            }
        }
    }
}
