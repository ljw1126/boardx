package com.example.hotarticle.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class ArticleCommentCountRepository {
    // hot-article::article::{articleId}::comment-count
    private static final String KEY_FORMAT = "hot-article::article::%s::comment-count";

    private final StringRedisTemplate redisTemplate;

    public Long read(Long articleId) {
        String result = redisTemplate.opsForValue().get(generateKey(articleId));
        return (result == null) ? 0L : Long.parseLong(result);
    }

    public void createOrUpdate(Long articleId, Long commentCount, Duration ttl) {
        redisTemplate.opsForValue().set(generateKey(articleId), String.valueOf(commentCount), ttl);
    }

    private String generateKey(Long articleId) {
        return KEY_FORMAT.formatted(articleId);
    }
}
