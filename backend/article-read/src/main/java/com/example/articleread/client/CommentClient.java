package com.example.articleread.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class CommentClient {
    private final RestClient restClient;

    public CommentClient(RestClient.Builder builder, @Value("${endpoints.board-comment-service.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public Long count(Long articleId) {
        try {
            return restClient.get()
                    .uri("/v2/comment/article/{articleId}/count", articleId)
                    .retrieve()
                    .body(Long.class);
        } catch (Exception e) {
            log.error("[CommentClient.count] articleId = {}", articleId, e);
            return 0L;
        }
    }
}
