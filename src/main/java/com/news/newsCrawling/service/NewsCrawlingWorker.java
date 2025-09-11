package com.news.newsCrawling.service;

import com.news.newsCrawling.model.contants.AGENT_ROLE;
import com.news.newsCrawling.model.vo.MessageVo;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.devtools.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NewsCrawlingWorker {

    @Value("${agent.role}")
    private String agentRole;

    @KafkaListener(topics = "news", groupId = "hb", containerFactory = "batchFactory")
    public void consume(List<MessageVo> messages) {
        if (AGENT_ROLE.WORKER.name().equalsIgnoreCase(agentRole)) {
            System.out.println("Consumed batch: " + messages);
            // 메시지 리스트 처리 로직 추가
            for (MessageVo message : messages) {
                System.out.println("Processing message: " + message);
            }
        }
    }
}
