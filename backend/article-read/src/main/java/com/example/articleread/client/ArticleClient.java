package com.example.articleread.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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

    public ArticlePageResponse readAll(Long boardId, Long page, Long pageSize) {
        try {
            return restClient.get()
                    .uri("/v1/article?boardId=%s&page=%s&pageSize=%s".formatted(boardId, page, pageSize))
                    .retrieve()
                    .body(ArticlePageResponse.class);
        } catch (Exception e) {
            log.error("[ArticlePageResponse.readAll] boardId = {}, page = {}, pageSize = {}", boardId, page, pageSize);
            return ArticlePageResponse.EMPTY;
        }
    }

    public List<ArticleResponse> readAllInfiniteScroll(Long boardId, Long lastArticleId, Long pageSize) {
        String uri = lastArticleId == null
                ? "/v1/article/infinite-scroll?boardId=%s&pageSize=%s".formatted(boardId, pageSize)
                : "/v1/article/infinite-scroll?boardId=%s&lastArticleId=%s&pageSize=%s".formatted(boardId, lastArticleId, pageSize);

        try {
            return restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("[ArticlePageResponse.readAllInfiniteScroll] boardId = {}, lastArticleId = {}, pageSize = {}", boardId, lastArticleId, pageSize);
            return Collections.emptyList();
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ArticleResponse {
        private Long articleId;
        private String title;
        private String content;
        private Long boardId;
        private Long writerId;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticlePageResponse {
        private List<ArticleResponse> articles;
        private Long count;

        public static ArticlePageResponse EMPTY = new ArticlePageResponse(List.of(), 0L);
    }
}
