package com.news.newsCrawling.service;

import com.news.newsCrawling.model.common.SearchDto;
import com.news.newsCrawling.model.vo.KeywordVo;
import com.news.newsCrawling.model.vo.NewsDataVo;

import java.time.LocalDateTime;
import java.util.List;

public interface NewsCrawlingService {
    int test();

    // 전체 데이터 builk insert
    void saveAll(List<NewsDataVo> savedList);

    // 코퍼스 데이터 생성
    List<String> makeCorpus(LocalDateTime localDateTime, int i);

    //아이디로 뉴스데이터 선택
    NewsDataVo selectContentById(long l);

    // 키워드 저장
    void insertKeywords(List<KeywordVo> keywordVoList);

    // 키워드 기반 검색
    List<NewsDataVo> searchByKeyword(SearchDto searchDto);

    // 일별 키워드 검색
    List<String> dailyKeyword();

    // 사용자 인기 검색
    List<NewsDataVo> searchByPopular(SearchDto searchDto);

    // 주간 키워드 검색
    List<String> weeklyKeyword();

    // 요약 및 임베딩 업데이트
    void updateSummaryAndEmbedding(NewsDataVo result);
}
