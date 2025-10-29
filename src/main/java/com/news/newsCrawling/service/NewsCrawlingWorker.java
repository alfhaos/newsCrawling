package com.news.newsCrawling.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.newsCrawling.model.contants.AGENT_ROLE;
import com.news.newsCrawling.model.contants.COMMAND_SITE_TYPE;
import com.news.newsCrawling.model.vo.MessageVo;
import com.news.newsCrawling.service.command.CommandFactory;
import com.news.newsCrawling.service.command.CommandInterface;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "agent.role", havingValue = "WORKER")
public class NewsCrawlingWorker {

    @Value("${agent.role}")
    private String agentRole;

    private final CommandFactory commandFactory;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    private CommandInterface daumCommand;
    private CommandInterface recursiveDaumCommand;

    @PostConstruct
    public void configureListener() {
        initializeCommands();
    }

    private void initializeCommands() {
        daumCommand = commandFactory.getCommand(COMMAND_SITE_TYPE.DAUM.getValue());
        recursiveDaumCommand = commandFactory.getCommand(COMMAND_SITE_TYPE.RECURSIVE_DAUM.getValue());
    }
    @KafkaListener(id = "newsCrawlingListener", topics = "newsCrawling", groupId = "hb", containerFactory = "batchFactory")
    public void consume(List<String> messages) {
        System.out.println("Consumed batch: " + messages);
        Map<Boolean, List<MessageVo>> partitionedMessages = messages.stream()
                .map(message -> {
                    try {
                        return objectMapper.readValue(message, MessageVo.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null; // 예외 발생 시 null 반환
                    }
                })
                .filter(messageVo -> messageVo != null) // null 필터링
                .collect(Collectors.partitioningBy(messageVo -> messageVo.getDepth() == 1));
        // depth가 1인 메인기사 리스트
        List<MessageVo> depth1Messages = partitionedMessages.get(true);
        // depth가 2인 리스트 사이드기사 리스트
        List<MessageVo> depth2Messages = partitionedMessages.get(false); 

        // 기사 저장
        try {
            // 메인기사 저장
            daumCommand.saveToDatabase(depth1Messages);
            // 사이드 기사 저장
            recursiveDaumCommand.saveToDatabase(depth2Messages);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
