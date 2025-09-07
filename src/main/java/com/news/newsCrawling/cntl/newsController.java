package com.news.newsCrawling.cntl;

import com.news.newsCrawling.model.command.CommandFactory;
import com.news.newsCrawling.model.command.CommandInterface;
import com.news.newsCrawling.model.contants.COMMAND_SITE_TYPE;
import com.news.newsCrawling.model.vo.MessageVo;
import com.news.newsCrawling.service.NewscrawlingService;
import com.news.newsCrawling.util.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class newsController {
    private final NewscrawlingService newscrawlingService;
    private final CommandFactory commandFactory;


    @GetMapping("/")
    private CommonResponse<Object> test() throws Exception {

        int test = newscrawlingService.test();
        CommandInterface command = commandFactory.getCommand(COMMAND_SITE_TYPE.DAUM.getValue());
        MessageVo messageVo = MessageVo.builder()
                .url("https://news.daum.net/")
                .type(COMMAND_SITE_TYPE.DAUM)
                .depth(1)
                .build();

        command.execute(messageVo);

        return new CommonResponse<>(null);
    }
}
