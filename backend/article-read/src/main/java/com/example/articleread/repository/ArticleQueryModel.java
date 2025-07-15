package com.example.articleread.repository;

import com.example.articleread.client.ArticleClient;
import com.example.event.paylod.ArticleCreatedEventPayload;
import com.example.event.paylod.ArticleLikedEventPayload;
import com.example.event.paylod.ArticleUnlikedEventPayload;
import com.example.event.paylod.ArticleUpdatedEventPayload;
import com.example.event.paylod.CommentCreatedEventPayload;
import com.example.event.paylod.CommentDeletedEventPayload;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArticleQueryModel {
    private Long articleId;
    private String title;
    private String content;
    private Long boardId;
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Long articleCommentCount;
    private Long articleLikeCount;

    public static ArticleQueryModel of(ArticleClient.ArticleResponse article, Long commentCount, Long likeCount) {
        ArticleQueryModel queryModel = new ArticleQueryModel();
        queryModel.articleId = article.getArticleId();
        queryModel.title = article.getTitle();
        queryModel.content = article.getContent();
        queryModel.boardId = article.getBoardId();
        queryModel.writerId = article.getWriterId();
        queryModel.createdAt = article.getCreatedAt();
        queryModel.modifiedAt = article.getModifiedAt();
        queryModel.articleCommentCount = commentCount;
        queryModel.articleLikeCount = likeCount;

        return queryModel;
    }

    public static ArticleQueryModel from(ArticleCreatedEventPayload payload) {
        ArticleQueryModel queryModel = new ArticleQueryModel();
        queryModel.articleId = payload.getArticleId();
        queryModel.title = payload.getTitle();
        queryModel.content = payload.getContent();
        queryModel.boardId = payload.getBoardId();
        queryModel.writerId = payload.getWriterId();
        queryModel.createdAt = payload.getCreatedAt();
        queryModel.modifiedAt = payload.getModifiedAt();
        queryModel.articleCommentCount = 0L;
        queryModel.articleLikeCount = 0L;
        return queryModel;
    }

    public void updatedBy(ArticleUpdatedEventPayload payload) {
        this.title = payload.getTitle();
        this.content = payload.getContent();
        this.boardId = payload.getBoardId();
        this.writerId = payload.getWriterId();
        this.createdAt = payload.getCreatedAt();
        this.modifiedAt = payload.getModifiedAt();
    }

    public void updatedBy(CommentCreatedEventPayload payload) {
        this.articleCommentCount = payload.getArticleCommentCount();
    }

    public void updatedBy(CommentDeletedEventPayload payload) {
        this.articleCommentCount = payload.getArticleCommentCount();
    }

    public void updatedBy(ArticleLikedEventPayload payload) {
        this.articleLikeCount = payload.getArticleLikeCount();
    }

    public void updatedBy(ArticleUnlikedEventPayload payload) {
        this.articleLikeCount = payload.getArticleLikeCount();
    }

}
