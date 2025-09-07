package com.news.newsCrawling.model.command;

import com.news.newsCrawling.model.vo.MessageVo;
import com.news.newsCrawling.model.vo.NewsDataVo;

import java.io.IOException;
import java.util.List;

public interface CommandInterface {
    void execute(MessageVo message) throws Exception;

    void saveToDatabase(List<NewsDataVo> newsDataVo) throws IOException;
}
