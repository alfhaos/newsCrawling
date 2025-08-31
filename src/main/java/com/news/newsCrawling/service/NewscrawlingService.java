package com.news.newsCrawling.service;

import com.news.newsCrawling.model.vo.NewsDataVo;

import java.util.List;

public interface NewscrawlingService {
    int test();

    // 전체 데이터 builk insert
    void saveAll(List<NewsDataVo> savedList);
}
