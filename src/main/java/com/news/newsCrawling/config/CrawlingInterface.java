package com.news.newsCrawling.config;

import org.openqa.selenium.WebElement;

import java.util.List;

public interface CrawlingInterface {

    List<WebElement> fetchMainHtml(String url, String depth1) throws Exception;
    WebElement fetchHtml(String url, String cssSelector, String wrapperSelector) throws Exception;
}
