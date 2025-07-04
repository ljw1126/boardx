package com.example.like.service;

import com.example.like.entity.ArticleLike;
import com.example.like.entity.ArticleLikeCount;
import com.example.like.repository.ArticleLikeCountRepository;
import com.example.like.repository.ArticleLikeRepository;
import com.example.like.service.response.ArticleLikeResponse;
import com.example.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleLikeService {
    private final Snowflake snowflake = new Snowflake();

    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleLikeCountRepository articleLikeCountRepository;

    @Transactional(readOnly = true)
    public ArticleLikeResponse read(Long articleId, Long userId) {
        return articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .map(ArticleLikeResponse::from)
                .orElseThrow();
    }

    public void like(Long articleId, Long userId) {
        articleLikeRepository.save(ArticleLike.of(snowflake.nextId(), articleId, userId));

        int result = articleLikeCountRepository.increase(articleId);
        if (result == 0) {
            articleLikeCountRepository.save(ArticleLikeCount.of(articleId, 1L));
        }
    }

    public void unlike(Long articleId, Long userId) {
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLike -> {
                    articleLikeRepository.delete(articleLike);
                    articleLikeCountRepository.decrease(articleId);
                });
    }

    @Transactional(readOnly = true)
    public Long count(Long articleId) {
        return articleLikeCountRepository.findById(articleId)
                .map(ArticleLikeCount::getLikeCount)
                .orElse(0L);
    }
}
