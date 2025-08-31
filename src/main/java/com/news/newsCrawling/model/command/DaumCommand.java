package com.news.newsCrawling.model.command;

import com.news.newsCrawling.config.CrawlingSiteConfig;
import com.news.newsCrawling.config.CrawlingSiteConfig.Site;
import com.news.newsCrawling.model.contants.COMMAND_SITE_TYPE;
import com.news.newsCrawling.model.vo.NewsDataVo;
import com.news.newsCrawling.service.NewscrawlingService;
import com.news.newsCrawling.util.JsoupCrawlingUtil;
import com.news.newsCrawling.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DaumCommand implements CommandInterface {
    private final JsoupCrawlingUtil jsoupCrawlingUtil;
    private final CrawlingSiteConfig crawlingSiteConfig;
    private final RedisUtil redisUtil;
    private final NewscrawlingService newscrawlingService;


    // 메인 사이트 최초 추출
    @Override
    public void execute(String url) throws Exception {
        Site daumSite = crawlingSiteConfig.getSites().get(COMMAND_SITE_TYPE.DAUM.getMessage());
        LinkedHashMap<String, String> selectors = daumSite.getSelectors();
        Document doc = jsoupCrawlingUtil.fetchHtml(url);

        // 이시간 주요 뉴스 추출
        Element depth1EL = doc.selectFirst(selectors.get("depth1"));

        Elements depth2ELs = depth1EL.select(selectors.get("depth2"));

        List<NewsDataVo> list = NewsDataVo.dataFormatForMessage(depth2ELs, COMMAND_SITE_TYPE.DAUM);
        for (NewsDataVo newsDataVo : list) {
            if(redisUtil.getData(newsDataVo.getUrl()) == null) {
                // Redis에 저장
                redisUtil.saveData(newsDataVo.getUrl(), "");
                System.out.println("DaumCommand - New URL Found: " + newsDataVo.getUrl());
            } else {
                // 이미 처리된 URL인 경우 리스트에서 제거
                list.remove(newsDataVo);
                System.out.println("DaumCommand - URL Already Processed: " + newsDataVo.getUrl());
            }
        }
        
        // TODO: 카프카 메세지 전송 후 워커에서 DB에 저장해야 하지만 일단 바로 저장
        saveToDatabase(list);
    }

    @Override
    public void saveToDatabase(List<NewsDataVo> newsDataVo) throws IOException {
        List<NewsDataVo> savedList = new ArrayList<>();
        // 각 url 접속 후 내용 추출
        for (NewsDataVo dataVo : newsDataVo) {
            try {
                Document doc = jsoupCrawlingUtil.fetchHtml(dataVo.getUrl());
                Site daumSite = crawlingSiteConfig.getSites().get(COMMAND_SITE_TYPE.DAUM.getMessage());
                LinkedHashMap<String, String> dataSelectors = daumSite.getDataSelectors();

                Element wrapper = doc.selectFirst(dataSelectors.get("wrapper"));
                NewsDataVo detailData = NewsDataVo.daumDataDetailFormat(wrapper, dataSelectors);
                detailData.setUrl(dataVo.getUrl());
                detailData.setSiteType(COMMAND_SITE_TYPE.DAUM);
                savedList.add(detailData);

            } catch (Exception e) {
                System.err.println("Error processing URL: " + dataVo.getUrl());
                e.printStackTrace();
            }
        }

        // DB 저장
        newscrawlingService.saveAll(savedList);
    }
}
