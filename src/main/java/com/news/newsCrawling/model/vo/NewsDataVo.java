package com.news.newsCrawling.model.vo;

import com.news.newsCrawling.model.contants.COMMAND_SITE_TYPE;
import com.news.newsCrawling.util.CommonUtil;
import lombok.Builder;
import lombok.Data;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;

@Data
@Builder
public class NewsDataVo {

    private Long id;
    private String title;
    private String content;
    private String publisher;
    private String url;
    private String viewCnt;
    private String commentCnt;
    private LocalDateTime createAt;
    // 감정표현 - 추천해요 갯수
    private int recommandEmotionScore;
    // 감정표현 - 좋아요 갯수
    private int likeEmotionScore;
    // 감정표현 - 감동이에요 갯수
    private int impressedEmotionScore;
    // 감정표현 - 화나요 갯수
    private int angryEmotionScore;
    // 감정표현 - 슬퍼요 갯수
    private int sadEmotionScore;

    // 메세지 필터 처리를 위한 변수
    private int depth;
    private COMMAND_SITE_TYPE siteType;

    // 각 사이트별로 카프카메세지 포멧
    public static List<NewsDataVo> dataFormatForMessage(Elements elements, COMMAND_SITE_TYPE siteType) {

        if(siteType.equals(COMMAND_SITE_TYPE.DAUM)) {
            return daumDataFormat(elements);
        } else {
            return null;
        }
    }

    // 다음 사이트 뉴스 데이터 포멧
    private static List<NewsDataVo> daumDataFormat(Elements elements) {
        return elements.stream().map(element -> {
            String temp = element.select("a").attr("href");
            return NewsDataVo.builder()
                    .url(temp)
                    .depth(1)
                    .siteType(COMMAND_SITE_TYPE.DAUM)
                    .build();
        }).toList();
    }

    // 다음 사이트 뉴스 상세 데이터 포멧
     public static NewsDataVo daumDataDetailFormat(Element wrapper, LinkedHashMap<String, String> dataSelectors) {

        // 날짜 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. M. d. HH:mm");

        // 제목
        Element title = wrapper.selectFirst(dataSelectors.get("title"));

        // 제작사
        Element publisherParent = wrapper.selectFirst(dataSelectors.get("publisher"));
        Element publisher = publisherParent.selectFirst("> p"); // 바로 하위 자식인 p 태그 선택

        // 생성일
        Element createAt = wrapper.selectFirst(dataSelectors.get("createAt"));

        // 내용
        Element contentWrapper = wrapper.selectFirst(dataSelectors.get("content"));

        StringBuilder contentBuilder = new StringBuilder();
        Elements contentP = contentWrapper.select("section > p");
        for (Element element : contentP) {
            contentBuilder.append(element.text()).append("\n"); // 각 요소의 텍스트를 추가하고 줄바꿈
        }
        String content = contentBuilder.toString(); // 최종 문자열 생성
        return NewsDataVo.builder().
                title(title.text()).
                publisher(CommonUtil.extractPublisher(publisher.text())).
                createAt(LocalDateTime.parse(createAt.text(), formatter)).
                content(content).
                build();
    }
}
