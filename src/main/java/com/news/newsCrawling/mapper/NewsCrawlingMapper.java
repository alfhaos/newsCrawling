package com.news.newsCrawling.mapper;

import com.news.newsCrawling.model.vo.KeywordVo;
import com.news.newsCrawling.model.vo.NewsDataVo;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface NewsCrawlingMapper{
    void saveAll(List<NewsDataVo> savedList);

    List<String> selectCorpusList(LocalDateTime localDateTime, int limit);

    NewsDataVo selectContentById(long id);

    void insertKeywords(List<KeywordVo> keywordVo);
}
