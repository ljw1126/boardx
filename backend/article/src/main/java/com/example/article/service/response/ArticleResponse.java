package com.example.article.service.response;

import com.example.article.entity.Article;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ArticleResponse {
    private Long articleId;
    private String title;
    private String content;
    private Long boardId;
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static ArticleResponse from(Article article) {
        return new ArticleResponse(
                article.getArticleId(),
                article.getTitle(),
                article.getContent(),
                article.getBoardId(),
                article.getWriterId(),
                article.getCreatedAt(),
                article.getModifiedAt()
        );
    }
}
