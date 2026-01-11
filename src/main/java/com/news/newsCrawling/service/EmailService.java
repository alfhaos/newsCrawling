package com.news.newsCrawling.service;

import com.news.newsCrawling.model.contants.SEARCH_DATE;
import com.news.newsCrawling.model.vo.NewsDataVo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${agent.target}")
    private String target;
    @Value("${spring.mail.username}") // 발신자 이메일 주소를 설정 파일에서 가져옴
    private String senderEmail;
    public void sendEmail(Map<String, List<NewsDataVo>> keywordNews
            , List<NewsDataVo> popularNews, SEARCH_DATE searchDate
            , String llmSummaryResult) throws MessagingException {
        // MimeMessage 생성
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(target);
        helper.setSubject(LocalDate.now() + (searchDate.equals(SEARCH_DATE.DAILY) ? " 오늘의 기사 요약" : " 주간 기사 요약"));
        helper.setFrom(senderEmail);

        // HTML 내용 생성
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<html>");
        htmlContent.append("<head>");
        htmlContent.append("<style>");
        htmlContent.append("body { font-family: Arial, sans-serif; line-height: 1.6; background-color: #f4f4f9; padding: 20px; }");
        htmlContent.append(".container { max-width: 800px; margin: 0 auto; background: #ffffff; border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); overflow: hidden; }");
        htmlContent.append(".header { background-color: #007BFF; color: #ffffff; text-align: center; padding: 20px; }");
        htmlContent.append(".header img { max-width: 100%; height: auto; }");
        htmlContent.append(".section { margin: 20px; padding: 20px; border-bottom: 1px solid #ddd; }");
        htmlContent.append(".section h2 { color: #007BFF; border-bottom: 2px solid #007BFF; padding-bottom: 5px; }");
        htmlContent.append(".news-item { margin: 10px 0; }");
        htmlContent.append(".news-item a { text-decoration: none; color: #333; font-weight: bold; }");
        htmlContent.append(".news-item a:hover { color: #007BFF; }");
        htmlContent.append(".rank { font-weight: bold; color: #007BFF; margin-right: 10px; }");
        htmlContent.append("</style>");
        htmlContent.append("</head>");
        htmlContent.append("<body>");
        htmlContent.append("<div class='container'>");

        // 헤더 섹션
        htmlContent.append("<div class='header'>");
        htmlContent.append("<h1>").append(searchDate.equals(SEARCH_DATE.DAILY) ? " 오늘의 뉴스 요약" : " 주간 뉴스 요약").append("</h1>");
        htmlContent.append("<p style='margin-top: 10px; font-size: 16px; color: #f1f1f1;'>")
                .append(searchDate.equals(SEARCH_DATE.DAILY) ? "AI가 분석한 일간 트렌드 입니다:" : "AI가 분석한 주간 트렌드 입니다:")
                .append("</p>");
        htmlContent.append("<p style='margin-top: 5px; font-size: 14px; color: #ffffff;'>").append(llmSummaryResult).append("</p>");
        htmlContent.append("</div>");

        // 키워드별 뉴스 섹션
        htmlContent.append("<div class='section'>");
        htmlContent.append("<h2>키워드별 뉴스</h2>");
        for (Map.Entry<String, List<NewsDataVo>> entry : keywordNews.entrySet()) {
            htmlContent.append("<div class='keyword-section' style='margin-bottom: 20px; padding: 15px; background-color: #f9f9f9; border-radius: 8px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);'>");
            htmlContent.append("<h3 style='color: #007BFF; margin-bottom: 10px;'>").append(entry.getKey()).append("</h3>");
            for (NewsDataVo news : entry.getValue()) {
                htmlContent.append("<div class='news-item' style='margin: 10px 0; padding: 10px; border: 1px solid #ddd; border-radius: 5px;'>");
                htmlContent.append("<a href='").append(news.getUrl()).append("' style='text-decoration: none; color: #333; font-weight: bold;'>")
                        .append(news.getTitle()).append("</a>");
                htmlContent.append("</div>");
            }
            htmlContent.append("</div>");
        }
        htmlContent.append("</div>");

        // 감정 표현이 많이 표출된 뉴스 섹션
        htmlContent.append("<div class='section'>");
        htmlContent.append("<h2>가장 핫 한 뉴스</h2>");
        int rank = 1;
        for (NewsDataVo news : popularNews) {
            htmlContent.append("<div class='news-item'>");
            htmlContent.append("<span class='rank'>").append(rank).append("위</span>");
            htmlContent.append("<a href='").append(news.getUrl()).append("'>").append(news.getTitle()).append("</a>");
            htmlContent.append("</div>");
            rank++;
        }
        htmlContent.append("</div>");

        htmlContent.append("</div>");
        htmlContent.append("</body>");
        htmlContent.append("</html>");

        // HTML 내용 설정
        helper.setText(htmlContent.toString(), true);

        // 이메일 전송
        mailSender.send(message);
    }
}
