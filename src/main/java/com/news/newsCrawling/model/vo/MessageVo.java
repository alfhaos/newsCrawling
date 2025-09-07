package com.news.newsCrawling.model.vo;

import com.news.newsCrawling.model.contants.COMMAND_SITE_TYPE;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageVo {

    private int depth;
    private String url;
    private COMMAND_SITE_TYPE type;

}
