package com.news.newsCrawling.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.news.newsCrawling.model.contants.COMMAND_SITE_TYPE;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@ToString
public class MessageVo implements Serializable {

    private int depth;
    private String url;
    private COMMAND_SITE_TYPE siteType;

    @JsonCreator
    public MessageVo(
            @JsonProperty("depth") int depth,
            @JsonProperty("url") String url,
            @JsonProperty("siteType") COMMAND_SITE_TYPE siteType) {
        this.depth = depth;
        this.url = url;
        this.siteType = siteType;
    }
    // 각 사이트별로 카프카메세지 포멧
    public static List<MessageVo> dataFormatForMessage(List<WebElement> elements, COMMAND_SITE_TYPE siteType) {
        if (siteType.equals(COMMAND_SITE_TYPE.DAUM)) {
            return daumDataFormat(elements);
        } else {
            return null;
        }
    }

    // 다음 사이트 뉴스 데이터 포멧
    private static List<MessageVo> daumDataFormat(List<WebElement> elements) {
        return elements.stream().map(element -> {
            String temp = element.findElement(By.tagName("a")).getAttribute("href");
            return MessageVo.builder()
                    .url(temp)
                    .depth(1)
                    .siteType(COMMAND_SITE_TYPE.DAUM)
                    .build();
        }).toList();
    }
}
