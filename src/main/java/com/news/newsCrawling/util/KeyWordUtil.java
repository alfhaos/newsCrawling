package com.news.newsCrawling.util;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
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
    private static final int topN = 3;
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
        ByteBuffersDirectory ramDirectory = new ByteBuffersDirectory();
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(ramDirectory, config);

        // 코퍼스 문서 추가
        for (String doc : corpus) {
            Document luceneDoc = new Document();
            luceneDoc.add(new TextField("content", doc, TextField.Store.NO));
            writer.addDocument(luceneDoc);
        }
        writer.close();

        // 인덱스 리더 생성
        IndexReader reader = DirectoryReader.open(ramDirectory);
        ClassicSimilarity similarity = new ClassicSimilarity();

        // 단일 문서의 TF-IDF 계산
        Map<String, Float> tfidfScores = new HashMap<>();
        Terms terms = reader.getTermVector(0, "content");
        if (terms != null) {
            TermsEnum termsEnum = terms.iterator();
            BytesRef term;
            while ((term = termsEnum.next()) != null) {
                String termText = term.utf8ToString();
                float tf = similarity.tf(termsEnum.totalTermFreq());
                float idf = similarity.idf(termsEnum.docFreq(), reader.numDocs());
                tfidfScores.put(termText, tf * idf);
            }
        }
        reader.close();

        // TF-IDF 점수 기준 상위 N개 키워드 추출
        return tfidfScores.entrySet().stream()
                .sorted((e1, e2) -> Float.compare(e2.getValue(), e1.getValue()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .toList();
    }
}
