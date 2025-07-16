package com.example.hotarticle.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.random.RandomGenerator;

@Disabled
public class DataInitializer {
    RestClient articleServiceClient = RestClient.create("http://localhost:9000");
    RestClient commentServiceClient = RestClient.create("http://localhost:9001");
    RestClient likeServiceClient = RestClient.create("http://localhost:9002");
    RestClient viewServiceClient = RestClient.create("http://localhost:9003");

    @Test
    void initialize() {
        for(int i=0; i< 30; i++) {
            Long articleId = createArticle(i);
            long commentCount = 1 + RandomGenerator.getDefault().nextLong(10);
            long likeCount = 1 + RandomGenerator.getDefault().nextLong(10);
            long viewCount = 1 + RandomGenerator.getDefault().nextLong(200);

            System.out.printf("articleId = %d, comment=%d, like=%d, view=%d%n", articleId, commentCount, likeCount, viewCount);

            createComment(articleId, commentCount);
            like(articleId, likeCount);
            view(articleId, viewCount);
        }
    }

    Long createArticle(int no) {
        return articleServiceClient.post()
                .uri("/v1/article")
                .body(new ArticleCreateRequest("title" + no, "content" + no, 1L, 1L))
                .retrieve()
                .body(ArticleResponse.class)
                .getArticleId();
    }

    @Getter
    @AllArgsConstructor
    static class ArticleCreateRequest {
        private String title;
        private String content;
        private Long writerId;
        private Long boardId;
    }

    @Getter
    static class ArticleResponse {
        private Long articleId;
    }

    void createComment(Long articleId, long commentCount) {
        for(int i = 1; i <= commentCount; i++) {
            commentServiceClient.post()
                    .uri("/v2/comment")
                    .body(new CommentCreateRequest(articleId, "content", (long) i))
                    .retrieve()
                    .body(CommentCreateResponse.class);

            commentCount -= 1;
        }
    }

    @Getter
    @AllArgsConstructor
    static class CommentCreateRequest {
        private Long articleId;
        private String content;
        private Long writerId;
    }

    @Getter
    @ToString
    @AllArgsConstructor
    static class CommentCreateResponse {
        private Long commentId;
        private String content;
        private Long articleId;
        private Long writerId;
        private Boolean deleted;
        private String path;
        private LocalDateTime createdAt;
    }

    void like(Long articleId, long likeCount) {
        for(int i = 1; i <= likeCount; i++) {
            ResponseEntity<Void> response = likeServiceClient.post()
                    .uri("/v1/article-like/article/{articleId}/user/{userId}", articleId, i)
                    .retrieve()
                    .toBodilessEntity();
        }
    }

    void view(Long articleId, long viewCount) {
        for(int i = 1; i <= viewCount; i++) {
            viewServiceClient.post()
                    .uri("/v1/article-view/article/{articleId}/user/{userId}", articleId, i)
                    .retrieve()
                    .toBodilessEntity();
        }
    }
}
