package com.example.hotarticle.service;

import com.example.hotarticle.repository.ArticleCommentCountRepository;
import com.example.hotarticle.repository.ArticleLikeCountRepository;
import com.example.hotarticle.repository.ArticleViewCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HotArticleScoreCalculator {
    private static final long ARTICLE_LIKE_COUNT_WEIGHT = 3;
    private static final long ARTICLE_COMMENT_COUNT_WEIGHT = 2;
    private static final long ARTICLE_VIEW_COUNT_WEIGHT = 1;

    private final ArticleLikeCountRepository articleLikeCountRepository;
    private final ArticleCommentCountRepository articleCommentCountRepository;
    private final ArticleViewCountRepository articleViewCountRepository;

    public long calculate(Long articleId) {
        Long likeCount = articleLikeCountRepository.read(articleId);
        Long commentCount = articleCommentCountRepository.read(articleId);
        Long viewCount = articleViewCountRepository.read(articleId);

        return likeCount * ARTICLE_LIKE_COUNT_WEIGHT
                + commentCount * ARTICLE_COMMENT_COUNT_WEIGHT
                + viewCount * ARTICLE_VIEW_COUNT_WEIGHT;
    }
}
