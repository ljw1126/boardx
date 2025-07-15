package com.example.articleread.client;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
public class ArticleClient {
    private final RestClient restClient;

    public ArticleClient(RestClient.Builder builder, @Value("${endpoints.board-article-service.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public Optional<ArticleResponse> read(Long articleId) {
        try {
            ArticleResponse response = restClient.get()
                    .uri("/v1/article/{articleId}", articleId)
                    .retrieve()
                    .body(ArticleResponse.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.error("[ArticleClient.read] articleId = {}", articleId, e);
            return Optional.empty();
        }
    }

    public Long count(Long boardId) {
        try {
            return restClient.get()
                    .uri("/v1/article/board/{boardId}/count", boardId)
                    .retrieve()
                    .body(Long.class);
        } catch (Exception e) {
            log.error("[ArticleClient.count] boardId = {}", boardId, e);
            return 0L;
        }
    }

    // TODO. 페이징 목록 조회, 무한 스크롤 목록 조회 추가

    @Getter
    public static class ArticleResponse {
        private Long articleId;
        private String title;
        private String content;
        private Long boardId;
        private Long writerId;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }

}
