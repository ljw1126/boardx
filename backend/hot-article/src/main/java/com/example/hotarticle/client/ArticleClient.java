package com.example.hotarticle.client;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleClient {
    private RestClient restClient;

    @Value("${endpoints.board-article-service.url}")
    private String articleServiceUrl;

    @PostConstruct
    void init() {
        this.restClient = RestClient.create(articleServiceUrl);
    }

    public ArticleResponse of(Long articleId) {
        try {
            return restClient.get()
                    .uri("/v1/article/{articleId}", articleId)
                    .retrieve()
                    .body(ArticleResponse.class);
        } catch (Exception e) {
            log.error("[ArticleClient.read] articleId = {}", articleId, e);
            return null;
        }
    }

    @Getter
    public static class ArticleResponse {
        private Long articleId;
        private String title;
        private LocalDateTime createdAt;
    }
}
