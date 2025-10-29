package com.news.newsCrawling.config;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class AIConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;
    @Value("${spring.ai.openai.embedding.options.model}")
    private String model;

    @Bean
    @Primary
    public DataSource defaultDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5432/crawling")
                .username("root")
                .password("root")
                .driverClassName("org.postgresql.Driver")
                .build();
    }
    @Bean
    public DataSource pgVectorDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5433/postgres")
                .username("postgres")
                .password("mypass")
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    @Bean
    public OpenAiApi openAiApi() {
        return OpenAiApi.builder()
                .apiKey(apiKey)
                .build();
    }
    @Bean
    public OpenAiEmbeddingModel embeddingModel(OpenAiApi openAiApi) {
        return new OpenAiEmbeddingModel(
                openAiApi,
                MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder()
                        .model(model)
                        .build(),
                RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }
}