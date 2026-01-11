package com.news.newsCrawling.model.vo;

import com.news.newsCrawling.model.common.TextSegmentDto;
import com.news.newsCrawling.model.contants.COMMAND_SITE_TYPE;
import com.news.newsCrawling.util.CommonUtil;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import lombok.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class NewsDataVo {

    private Long id;
    private String title;
    private String content;
    private String publisher;
    private String url;
    private String viewCnt;
    private int commentCnt;
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

    // llm 활용 요약 내용
    private String summaryContent;
    // 검색, 키워드 추천용 임베딩 벡터
    private float[] embedding;

    // 유사도 점수 필드 추가
    private Double similarity;

    // 각 사이트별로 카프카메세지 포멧
    public static List<NewsDataVo> dataFormatForMessage(List<WebElement> elements, COMMAND_SITE_TYPE siteType) {
        if (siteType.equals(COMMAND_SITE_TYPE.DAUM)) {
            return daumDataFormat(elements);
        } else {
            return null;
        }
    }

    // 다음 사이트 뉴스 데이터 포멧
    private static List<NewsDataVo> daumDataFormat(List<WebElement> elements) {
        return elements.stream().map(element -> {
            String temp = element.findElement(By.tagName("a")).getAttribute("href");
            return NewsDataVo.builder()
                    .url(temp)
                    .depth(1)
                    .siteType(COMMAND_SITE_TYPE.DAUM)
                    .build();
        }).toList();
    }

    // 다음 사이트 뉴스 상세 데이터 포멧
    public static NewsDataVo daumDataDetailFormat(WebElement wrapper, LinkedHashMap<String, String> dataSelectors) {
        // 날짜 변환 포맷
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. M. d. HH:mm");

        // 제목 추출
        WebElement title = wrapper.findElement(By.cssSelector(dataSelectors.get("title")));

        String publisherText = "";
        try {
            // 제작사 추출
            WebElement publisher = wrapper.findElement(By.cssSelector(dataSelectors.get("publisher")));
            publisherText = publisher.getText();
        } catch (NoSuchElementException e) {
            // 제작사 정보가 없을 경우 빈 문자열로 설정
        }

        // 생성일 추출
        WebElement createAt = wrapper.findElement(By.cssSelector(dataSelectors.get("createAt")));

        // 댓글 갯수 추출
        int commentCount = 0; // 기본값 0으로 초기화
        try {
            WebElement comment = wrapper.findElement(By.cssSelector(dataSelectors.get("comment")));
            String commentText = comment.getText();
            // 정규식을 사용하여 숫자만 추출
            String number = commentText.replaceAll("[^0-9]", "");
            commentCount = number.isEmpty() ? 0 : Integer.parseInt(number);
        } catch (NoSuchElementException e) {
            // 요소가 없을 경우 기본값 0 유지
        }
        // 이모티콘 추출
        WebElement emoticon = wrapper.findElement(By.cssSelector(dataSelectors.get("emoticon")));
        List<WebElement> emoticonList = emoticon.findElements(By.cssSelector("span.count_label"));
        // 감정표현 점수 설정
        

        // 내용 추출
        WebElement contentWrapper = wrapper.findElement(By.cssSelector(dataSelectors.get("content")));
        List<WebElement> contentP = contentWrapper.findElements(By.cssSelector("section > p"));

        // 내용 텍스트 합치기
        StringBuilder contentBuilder = new StringBuilder();
        for (WebElement element : contentP) {
            contentBuilder.append(element.getText()).append("\n");
        }

        String content = contentBuilder.toString();

        // NewsDataVo 객체 생성 및 반환
        return NewsDataVo.builder()
                .title(title.getText())
                .publisher(CommonUtil.extractPublisher(publisherText))
                .createAt(LocalDateTime.parse(createAt.getText(), formatter))
                .content(content)
                .commentCnt(commentCount)
                .recommandEmotionScore(Integer.parseInt(emoticonList.get(0).getText()))
                .likeEmotionScore(Integer.parseInt(emoticonList.get(1).getText()))
                .impressedEmotionScore(Integer.parseInt(emoticonList.get(2).getText()))
                .angryEmotionScore(Integer.parseInt(emoticonList.get(3).getText()))
                .sadEmotionScore(Integer.parseInt(emoticonList.get(4).getText()))
                .build();
    }
    public static TextSegmentDto convertToTextSegment(NewsDataVo newsData, String keyword) {
        if (newsData.getContent() == null || newsData.getContent().isEmpty()) {
            return null; // content가 null이거나 빈 값인 경우 null 반환
        }

        // 메타데이터 생성
        Metadata metadata = new Metadata();
        metadata.put("title", newsData.getTitle());
        metadata.put("summary", newsData.getSummaryContent());
        metadata.put("createdAt", newsData.getCreateAt().toString());
        metadata.put("keyword", keyword);

        // TextSegment 생성
        TextSegment textSegment = new TextSegment(newsData.getContent(), metadata);

        // TextSegmentDto 생성 및 반환
        return new TextSegmentDto(newsData.getId(), textSegment);
    }

    public void setSummaryContent(String summaryContent) {
        this.summaryContent = summaryContent;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setSiteType(COMMAND_SITE_TYPE siteType) {
        this.siteType = siteType;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
