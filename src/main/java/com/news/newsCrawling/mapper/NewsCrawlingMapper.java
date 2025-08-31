package com.news.newsCrawling.mapper;

import com.news.newsCrawling.model.vo.NewsDataVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NewsCrawlingMapper{

    int test();

    void saveAll(List<NewsDataVo> savedList);
}
