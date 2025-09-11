package com.news.newsCrawling.service.command;

import com.news.newsCrawling.model.vo.MessageVo;
import com.news.newsCrawling.model.vo.NewsDataVo;

import java.io.IOException;
import java.util.List;

public interface CommandInterface {
    void execute(MessageVo message) throws Exception;

    void saveToDatabase(List<MessageVo> newsDataVo) throws IOException;
}
