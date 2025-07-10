package com.example.view.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "article_view_count")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleViewCount {
    @Id
    private Long articleId;
    private Long viewCount;

    public static ArticleViewCount of(Long articleId, Long viewCount) {
        ArticleViewCount articleViewCount = new ArticleViewCount();
        articleViewCount.articleId = articleId;
        articleViewCount.viewCount = viewCount;
        return articleViewCount;
    }
}
