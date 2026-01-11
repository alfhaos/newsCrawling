package com.news.newsCrawling.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
@Component
@RequiredArgsConstructor
public class RedisUtil {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String corpusKey = "corpus";

    // 데이터 저장 및 유효시간 설정
    public void saveData(String key, String value) {
        redisTemplate.opsForValue().set(key, value, 30, TimeUnit.MINUTES); // 30분 유효시간 설정
    }

    // 데이터 조회
    public String getData(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    // 코퍼스 데이터
    public List<String> getCorpus() {
        return (List<String>) redisTemplate.opsForValue().get(corpusKey);
    }

    // 코퍼스 데이터 저장 및 유효시간 설정
    public void saveCorpusData(List<String> value) {
        redisTemplate.opsForValue().set(corpusKey, value, 60, TimeUnit.MINUTES); // 60분 유효시간 설정
    }

}
