package com.news.newsCrawling.util;

import com.news.newsCrawling.config.CrawlingInterface;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class SeleniumCrawlingUtil implements CrawlingInterface {
    public static WebDriver driver;
    public static final String allowAll = "//*";

    static {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }
    @Override
    public List<WebElement> fetchMainHtml(String url, String depth1) throws Exception {
        driver.get(url);
        // 모든 요소 반환
        return driver.findElements(By.cssSelector(depth1));
    }

    // 기사 상세 조회
    @Override
    public WebElement fetchHtml(String url, String cssSelector, String wrapperSelector) throws Exception {
        driver.get(url);
        // 10초 대기
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        // 특정 요소(댓글 등 동적로딩되는 요소) 로딩 대기
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(cssSelector)));
        // 모든 요소 반환
        return driver.findElement(By.cssSelector(wrapperSelector));
    }

    // 사이드 영역 기사 추출
    @Override
    public WebElement fetchHtml(String url, String wrapperSelector) throws Exception {
        driver.get(url);
        // 10초 대기
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        // 모든 요소 반환
        return driver.findElement(By.cssSelector(wrapperSelector));
    }
}
