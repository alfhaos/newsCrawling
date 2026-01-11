package com.news.newsCrawling.model.vo;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class KeywordVo {

    private Long id;
    private String keyword;
    private LocalDateTime createAt;
}
