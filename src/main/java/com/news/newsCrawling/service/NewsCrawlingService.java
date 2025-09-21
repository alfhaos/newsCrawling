package com.news.newsCrawling.service;

import com.news.newsCrawling.model.vo.NewsDataVo;

import java.time.LocalDateTime;
import java.util.List;

public interface NewsCrawlingService {
    int test();

    // 전체 데이터 builk insert
    void saveAll(List<NewsDataVo> savedList);

    // 코퍼스 데이터 생성
    List<String> makeCorpus(LocalDateTime localDateTime, int i);
}
