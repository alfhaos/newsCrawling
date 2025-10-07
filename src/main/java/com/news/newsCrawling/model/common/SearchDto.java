package com.news.newsCrawling.model.common;

import com.news.newsCrawling.model.contants.SEARCH_TYPE;
import lombok.Data;

@Data
public class SearchDto {

    private String keyword;
    private String title;
    private String content;
    private SEARCH_TYPE searchType;

}
