package com.example.hotarticle.service.response;

import com.example.hotarticle.client.ArticleClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HotArticleResponse {
    private Long articleId;
    private String title;
    private LocalDateTime createdAt;

    public static HotArticleResponse from(ArticleClient.ArticleResponse articleResponse) {
        HotArticleResponse response = new HotArticleResponse();
        response.articleId = articleResponse.getArticleId();
        response.title = articleResponse.getTitle();
        response.createdAt = articleResponse.getCreatedAt();
        return response;
    }
}
