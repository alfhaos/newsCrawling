package com.news.newsCrawling.util;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.regex.Pattern;
@Component
public class KoreanPostpositionRemover {

    // 모든 조사 패턴 정의
    private static final Pattern POSTPOSITION_PATTERN = Pattern.compile("(은|는|이|가|를|을|에|의|로|과|와|도|에서|으로|한테|께|밖에|마다|부터|까지|처럼|같이|보다|든지|라도|만큼|이나|나|야|며|든|뿐|있다|했다)$");

    // 조사 제거 메서드
    public static List<String> removePostposition(List<String> keywords) {
        for (int i = 0; i < keywords.size(); i++) {
            String keyword = keywords.get(i);
            keywords.set(i, POSTPOSITION_PATTERN.matcher(keyword).replaceAll(""));
        }
        return keywords;
    }
}
