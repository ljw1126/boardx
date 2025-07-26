package com.example.articleread.service.response;

import com.example.articleread.repository.ArticleQueryModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ArticleReadResponse {
    private Long articleId;
    private String title;
    private String content;
    private Long boardId;
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Long articleCommentCount;
    private Long articleLikeCount;
    private Long articleViewCount;

    public static ArticleReadResponse of(ArticleQueryModel articleQueryModel, Long viewCount) {
        ArticleReadResponse response = new ArticleReadResponse();
        response.articleId = articleQueryModel.getArticleId();
        response.title = articleQueryModel.getTitle();
        response.content = articleQueryModel.getContent();
        response.boardId = articleQueryModel.getBoardId();
        response.writerId = articleQueryModel.getWriterId();
        response.createdAt = articleQueryModel.getCreatedAt();
        response.modifiedAt = articleQueryModel.getModifiedAt();
        response.articleCommentCount = articleQueryModel.getArticleCommentCount();
        response.articleLikeCount = articleQueryModel.getArticleLikeCount();
        response.articleViewCount = viewCount;
        return response;
    }
}
