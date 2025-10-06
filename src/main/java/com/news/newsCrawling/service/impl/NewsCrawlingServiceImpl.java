package com.news.newsCrawling.service.impl;

import com.news.newsCrawling.mapper.NewsCrawlingMapper;
import com.news.newsCrawling.model.vo.KeywordVo;
import com.news.newsCrawling.model.vo.NewsDataVo;
import com.news.newsCrawling.service.NewsCrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsCrawlingServiceImpl implements NewsCrawlingService {

    private final NewsCrawlingMapper newscrawlingMapper;

    @Override
    public int test() {
        return 0;
    }

    @Override
    public void saveAll(List<NewsDataVo> savedList) {
        newscrawlingMapper.saveAll(savedList);
    }

    @Override
    public List<String> makeCorpus(LocalDateTime localDateTime, int limit) {
        return newscrawlingMapper.selectCorpusList(localDateTime, limit);
    }

    @Override
    public NewsDataVo selectContentById(long id) {
        return newscrawlingMapper.selectContentById(id);
    }

    @Override
    public void insertKeywords(List<KeywordVo> keywordVo) {
        newscrawlingMapper.insertKeywords(keywordVo);
    }
}
