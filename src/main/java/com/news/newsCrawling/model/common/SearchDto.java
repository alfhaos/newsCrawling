package com.news.newsCrawling.model.common;

import com.news.newsCrawling.model.contants.SEARCH_DATE;
import com.news.newsCrawling.model.contants.SEARCH_TYPE;
import lombok.Data;

@Data
public class SearchDto {

    private String keyword;
    private String title;
    private String content;
    private SEARCH_TYPE searchType;
    private SEARCH_DATE searchDate;

}
