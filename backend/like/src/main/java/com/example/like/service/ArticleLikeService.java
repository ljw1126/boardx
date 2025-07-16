package com.example.like.service;

import com.example.event.EventType;
import com.example.event.paylod.ArticleLikedEventPayload;
import com.example.event.paylod.ArticleUnlikedEventPayload;
import com.example.like.entity.ArticleLike;
import com.example.like.entity.ArticleLikeCount;
import com.example.like.repository.ArticleLikeCountRepository;
import com.example.like.repository.ArticleLikeRepository;
import com.example.like.service.response.ArticleLikeResponse;
import com.example.outboxmessagerelay.OutboxEventPublisher;
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
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional(readOnly = true)
    public ArticleLikeResponse read(Long articleId, Long userId) {
        return articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .map(ArticleLikeResponse::from)
                .orElseThrow();
    }

    // select .. for update + update
    public void like(Long articleId, Long userId) {
        ArticleLike saved = articleLikeRepository.save(ArticleLike.of(snowflake.nextId(), articleId, userId));

        ArticleLikeCount articleLikeCount = articleLikeCountRepository.findLockedByArticleId(articleId)
                .orElseGet(() -> ArticleLikeCount.of(articleId, 0L));

        articleLikeCount.increase();
        articleLikeCountRepository.save(articleLikeCount);

        outboxEventPublisher.publish(
                EventType.ARTICLE_LIKED,
                ArticleLikedEventPayload.builder()
                        .articleLikeId(saved.getArticleLikeId())
                        .articleId(saved.getArticleId())
                        .userId(saved.getUserId())
                        .createdAt(saved.getCreatedAt())
                        .articleLikeCount(count(saved.getArticleId()))
                        .build(),
                articleLikeCount.getArticleId()
        );
    }

    public void unlike(Long articleId, Long userId) {
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLike -> {
                    articleLikeRepository.delete(articleLike);
                    ArticleLikeCount articleLikeCount = articleLikeCountRepository.findLockedByArticleId(articleId).orElseThrow();
                    articleLikeCount.decrease();

                    outboxEventPublisher.publish(
                            EventType.ARTICLE_LIKED,
                            ArticleUnlikedEventPayload.builder()
                                    .articleLikeId(articleLike.getArticleLikeId())
                                    .articleId(articleLike.getArticleId())
                                    .userId(articleLike.getUserId())
                                    .createdAt(articleLike.getCreatedAt())
                                    .articleLikeCount(count(articleLike.getArticleId()))
                                    .build(),
                            articleId
                    );
                });
    }

    @Transactional(readOnly = true)
    public Long count(Long articleId) {
        return articleLikeCountRepository.findById(articleId)
                .map(ArticleLikeCount::getLikeCount)
                .orElse(0L);
    }
}
