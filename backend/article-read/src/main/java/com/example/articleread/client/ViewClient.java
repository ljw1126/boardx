package com.example.articleread.client;

import com.example.articleread.cache.OptimizedCacheable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class ViewClient {
    private final RestClient restClient;

    public ViewClient(RestClient.Builder builder, @Value("${endpoints.board-view-service.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @OptimizedCacheable(type = "articleViewCount", ttlSeconds = 1)
    public Long count(Long articleId) {
        try {
            return restClient.get()
                    .uri("/v1/article_view/article/{articleId}/count", articleId)
                    .retrieve()
                    .body(Long.class);
        } catch (Exception e) {
            log.error("[ViewClient.count] articleId = {}", articleId, e);
            return 0L;
        }
    }
}
