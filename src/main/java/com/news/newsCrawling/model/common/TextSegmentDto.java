package com.news.newsCrawling.model.common;

import dev.langchain4j.data.segment.TextSegment;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TextSegmentDto {
    private Long id;
    private TextSegment textSegment;
}
