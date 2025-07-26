package com.example.articleread.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class LikeClient {
    private final RestClient restClient;

    public LikeClient(RestClient.Builder builder, @Value("${endpoints.board-like-service.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public Long count(Long articleId) {
        try {
            return restClient.get()
                    .uri("/v1/article-like/article/{articleId}/count", articleId)
                    .retrieve()
                    .body(Long.class);
        } catch (Exception e) {
            log.error("[LikeClient.count] articleId = {}", articleId, e);
            return 0L;
        }
    }
}
