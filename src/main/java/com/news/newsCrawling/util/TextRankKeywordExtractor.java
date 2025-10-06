package com.news.newsCrawling.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
@Component
@RequiredArgsConstructor
public class TextRankKeywordExtractor {
    private final KoreanPostpositionRemover koreanPostpositionRemover;
    private static final int WINDOW_SIZE = 4; // 단어 간 연결 범위
    private static final double DAMPING_FACTOR = 0.85; // 감쇠 계수
    private static final int ITERATIONS = 50; // 반복 횟수
    private static final int TOP_N = 5; // 추출할 키워드 수

    public List<String> extractKeywords(String document) {
        String[] words = preprocess(document);
        Map<String, Set<String>> graph = buildGraph(words);
        Map<String, Double> scores = rank(graph);


        List<String> result = scores.entrySet().stream()
                .filter(entry -> entry.getKey().length() >= 2) // 2글자 이상 필터링
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(TOP_N)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        return KoreanPostpositionRemover.removePostposition(result);
    }

    private String[] preprocess(String document) {
        // 전처리: 소문자 변환, 특수문자 제거, 불용어 제거 등
        return document.toLowerCase()
                .replaceAll("[^\\p{L}\\p{Nd}\\s]", "")
                .split("\\s+");
    }

    private Map<String, Set<String>> buildGraph(String[] words) {
        Map<String, Set<String>> graph = new HashMap<>();
        for (int i = 0; i < words.length; i++) {
            graph.putIfAbsent(words[i], new HashSet<>());
            for (int j = Math.max(0, i - WINDOW_SIZE); j < Math.min(words.length, i + WINDOW_SIZE); j++) {
                if (i != j) {
                    graph.get(words[i]).add(words[j]);
                }
            }
        }
        return graph;
    }

    private Map<String, Double> rank(Map<String, Set<String>> graph) {
        Map<String, Double> scores = new HashMap<>();
        Map<String, Double> finalScores = scores;
        graph.keySet().forEach(word -> finalScores.put(word, 1.0));

        for (int i = 0; i < ITERATIONS; i++) {
            Map<String, Double> newScores = new HashMap<>();
            for (String word : graph.keySet()) {
                double score = 1 - DAMPING_FACTOR;
                for (String neighbor : graph.get(word)) {
                    score += DAMPING_FACTOR * (scores.get(neighbor) / graph.get(neighbor).size());
                }
                newScores.put(word, score);
            }
            scores = newScores;
        }
        return scores;
    }
}
