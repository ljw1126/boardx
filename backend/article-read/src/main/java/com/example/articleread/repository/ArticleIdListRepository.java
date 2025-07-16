package com.example.articleread.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ArticleIdListRepository {
    // article-read::board::{boardId}::article-list
    private static final String KEY_FORMAT = "article-read::board::%s::article-list";

    private final StringRedisTemplate redisTemplate;

    public void add(Long boardId, Long articleId, Long limit) {
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection connection = (StringRedisConnection) action;
            String key = generateKey(boardId);
            connection.zAdd(key, 0, toPaddedString(articleId));
            connection.zRemRange(key, 0, - limit - 1);
            return null;
        });
    }

    public void delete(Long boardId, Long articleId) {
        redisTemplate.opsForZSet().remove(generateKey(boardId), toPaddedString(articleId));
    }

    // TODO. 페이징 방식, 무한 스크롤 방식

    private String generateKey(Long boardId) {
        return KEY_FORMAT.formatted(boardId);
    }

    // 1234 -> 000_0000_0000_0000_1234
    private String toPaddedString(Long articleId) {
        return "%019d".formatted(articleId);
    }
}
