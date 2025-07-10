package com.example.view.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

// 조회수 어뷰징 방지 (10s)
@Repository
@RequiredArgsConstructor
public class ArticleViewDistributedLockRepository {
    private static final String KEY_FORMAT = "view::article::%s::user::%s::lock";

    private final StringRedisTemplate redisTemplate;

    public Boolean lock(Long articleId, Long userId, Duration ttl) {
        String key = generateKey(articleId, userId);
        return redisTemplate.opsForValue().setIfAbsent(key, "", ttl);
    }

    private String generateKey(Long articleId, Long userId) {
        return KEY_FORMAT.formatted(articleId, userId);
    }
}
