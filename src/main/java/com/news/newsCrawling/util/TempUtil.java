package com.news.newsCrawling.util;

import com.news.newsCrawling.service.command.CommandFactory;
import com.news.newsCrawling.service.command.CommandInterface;
import com.news.newsCrawling.model.contants.COMMAND_SITE_TYPE;
import com.news.newsCrawling.model.vo.MessageVo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TempUtil {
    @Lazy
    private final CommandFactory commandFactory;
    public void test(List<MessageVo> urlList) throws Exception {
        // TODO: 카프카에 재귀적 크롤링 메세지 전송
        CommandInterface command = commandFactory.getCommand(COMMAND_SITE_TYPE.RECURSIVE_DAUM.getValue());
        for(MessageVo vo: urlList){
            command.execute(vo);
        }
    }
}
