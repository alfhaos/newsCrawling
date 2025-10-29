package com.news.newsCrawling.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class CommonUtil {

    private static final String PUBLISHER_REGEX = "Copyright ©\\s(.*?)\\.";
    public static String extractPublisher(String text) {
        Pattern pattern = compile(PUBLISHER_REGEX);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1); // 첫 번째 그룹(출판사 이름) 반환
        }
        return "";
    }
}
