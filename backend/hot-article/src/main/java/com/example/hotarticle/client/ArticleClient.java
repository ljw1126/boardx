package com.example.hotarticle.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@Slf4j
@Component
public class ArticleClient {
    private final RestClient restClient;

    public ArticleClient(RestClient.Builder builder, @Value("${endpoints.board-article-service.url}") String articleServiceUrl) {
        this.restClient = builder.baseUrl(articleServiceUrl).build();
    }

    public ArticleResponse read(Long articleId) {
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
    @AllArgsConstructor
    public static class ArticleResponse {
        private Long articleId;
        private String title;
        private LocalDateTime createdAt;
    }
}
