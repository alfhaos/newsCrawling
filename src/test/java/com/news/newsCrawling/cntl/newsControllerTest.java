package com.news.newsCrawling.cntl;

import com.news.newsCrawling.config.CrawlingSiteConfig;
import com.news.newsCrawling.model.contants.COMMAND_SITE_TYPE;
import com.news.newsCrawling.service.NewscrawlingService;
import com.news.newsCrawling.util.RedisUtil;
import com.news.newsCrawling.util.SeleniumCrawlingUtil;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
@SpringBootTest
public class newsControllerTest {

    @Autowired
    private SeleniumCrawlingUtil seleniumCrawlingUtil;
    @Autowired
    private CrawlingSiteConfig crawlingSiteConfig;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private NewscrawlingService newscrawlingService;

    @Test
    @Rollback(value = true)
    @Transactional
    public void testExtractData() throws Exception {
        String url = "https://v.daum.net/v/20250831190224453";
        CrawlingSiteConfig.Site daumSite = crawlingSiteConfig.getSites().get(COMMAND_SITE_TYPE.DAUM.getMessage());
        LinkedHashMap<String, String> dataSelectors = daumSite.getDataSelectors();

        // HTML 가져오기
        WebElement doc = seleniumCrawlingUtil.fetchHtml(url, "div.inner_emoticon", "div.inner_emoticon");

        String temp = doc.getText();
        System.out.println(temp);
    }

    @Test
    @Rollback(value = true)
    @Transactional
    public void seleniumTest() throws Exception {
        String url = "https://v.daum.net/v/20250831190224453";

        WebDriver driver = SeleniumCrawlingUtil.driver;
        driver.get(url);
        Thread.sleep(5000); // 5초 대기
        // 3. 데이터 추출
        WebElement wrapper = driver.findElement(By.cssSelector("div.main-content"));
        System.out.println(wrapper.getText());
        List<WebElement> test = wrapper.findElements(By.cssSelector("div.inner_emoticon"));

        for (WebElement webElement : test) {
            String temp = webElement.getText();
            System.out.println(temp);
        }
    }

    @Test
    @Rollback(value = true)
    @Transactional
    public void seleniumMainTest() throws Exception {
        String url = "https://news.daum.net/";
        List<WebElement> result = seleniumCrawlingUtil.fetchMainHtml(url, "div.box_comp.box_news_headline2");

        for (WebElement webElement : result) {
            System.out.println(webElement.getText());
        }
    }
}