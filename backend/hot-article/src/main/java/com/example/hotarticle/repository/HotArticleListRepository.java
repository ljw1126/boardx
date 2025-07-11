package com.example.hotarticle.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class HotArticleListRepository {
    // hot-article::list::{yyyyMMdd}
    private static final String KEY_FORMAT = "hot-article::list::%s";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final StringRedisTemplate redisTemplate;

    public void add(Long articleId, LocalDateTime dateTime, Long score, Long limit, Duration ttl) {
        redisTemplate.executePipelined((RedisCallback<?>)  action -> {
            StringRedisConnection connection = (StringRedisConnection) action;
            String key = generateKey(dateTime);
            connection.zAdd(key, score, String.valueOf(articleId));
            connection.zRemRange(key, 0, - limit - 1);
            connection.expire(key, ttl.toSeconds());
            return null;
        });
    }

    private String generateKey(LocalDateTime dateTime) {
        return generateKey(TIME_FORMATTER.format(dateTime));
    }

    private String generateKey(String dateStr) {
        return KEY_FORMAT.formatted(dateStr);
    }

    // TODO. 제거
    public List<Long> readAll(LocalDateTime dateTime) {
        return redisTemplate.opsForZSet()
                .reverseRange(generateKey(dateTime), 0, -1)
                .stream()
                .peek(v -> log.info("[HotArticleListRepository.readAll] articleId = {}", v))
                .map(Long::valueOf)
                .toList();
    }

    // TODO 전환
    public List<Long> readAll(String dateStr) {
        return redisTemplate.opsForZSet()
                .reverseRange(generateKey(dateStr), 0, -1)
                .stream()
                .peek(v -> log.info("[HotArticleListRepository.readAll] articleId = {}", v))
                .map(Long::valueOf)
                .toList();
    }

    public void remove(Long articleId, LocalDateTime dateTime) {
        redisTemplate.opsForZSet().remove(generateKey(dateTime), String.valueOf(articleId));
    }
}
