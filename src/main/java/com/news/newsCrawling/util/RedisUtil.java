package com.news.newsCrawling.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {
    private final RedisTemplate<String, Object> redisTemplate;

    // 데이터 저장 및 유효시간 설정
    public void saveData(String key, String value) {
        redisTemplate.opsForValue().set(key, value, 30, TimeUnit.MINUTES); // 30분 유효시간 설정
    }

    // 데이터 조회
    public String getData(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }
}
