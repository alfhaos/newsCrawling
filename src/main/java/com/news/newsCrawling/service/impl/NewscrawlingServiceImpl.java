package com.news.newsCrawling.service.impl;

import com.news.newsCrawling.mapper.NewsCrawlingMapper;
import com.news.newsCrawling.model.vo.NewsDataVo;
import com.news.newsCrawling.service.NewscrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewscrawlingServiceImpl implements NewscrawlingService {

    private final NewsCrawlingMapper newscrawlingMapper;

    @Override
    public int test() {
        return newscrawlingMapper.test();
    }

    @Override
    public void saveAll(List<NewsDataVo> savedList) {
        newscrawlingMapper.saveAll(savedList);
    }
}
