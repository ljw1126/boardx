package com.example.comment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "article_comment_count")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ArticleCommentCount {
    @Id
    private Long articleId;
    private Long commentCount;

    public static ArticleCommentCount of(Long articleId, Long commentCount) {
        return new ArticleCommentCount(articleId, commentCount);
    }
}
