package com.news.newsCrawling.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class KeywordVo {

    private Long id;
    private String keyword;
    private LocalDateTime createAt;
}
