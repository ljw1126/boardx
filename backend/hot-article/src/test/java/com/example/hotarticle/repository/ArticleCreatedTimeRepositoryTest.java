package com.example.hotarticle.repository;

import com.example.hotarticle.EmbeddedRedis;
import com.example.hotarticle.config.KafkaConfig;
import com.example.hotarticle.consumer.HotArticleEventConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Import(EmbeddedRedis.class)
@ActiveProfiles("test")
@SpringBootTest
class ArticleCreatedTimeRepositoryTest {

    @Autowired
    private ArticleCreatedTimeRepository articleCreatedTimeRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // kafka 로그 제거
    @MockitoBean
    private KafkaConfig kafkaConfig;

    @MockitoBean
    private HotArticleEventConsumer hotArticleEventConsumer;

    @BeforeEach
    void setUp() {
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        RedisConnection connection = connectionFactory.getConnection();
        connection.serverCommands().flushAll();

        articleCreatedTimeRepository.createOrUpdate(1L, LocalDateTime.of(2025, 7, 1, 12, 0, 0), Duration.ofSeconds(30));
    }

    @Test
    void read() {
        Long articleId = 1L;

        LocalDateTime actual = articleCreatedTimeRepository.read(articleId);

        assertThat(actual).isEqualTo(LocalDateTime.of(2025, 7, 1, 12, 0, 0));
    }

    @Test
    void create() {
        Long articleId = 2L;
        LocalDateTime createdAt = LocalDateTime.of(2025, 7, 2, 0, 0, 0);
        Duration ttl = Duration.ofSeconds(30);

        articleCreatedTimeRepository.createOrUpdate(articleId, createdAt, ttl);

        LocalDateTime actual = articleCreatedTimeRepository.read(articleId);

        assertThat(actual).isEqualTo(createdAt);
    }

    @Test
    void update() {
        Long articleId = 1L;
        LocalDateTime createdAt = LocalDateTime.of(2025, 7, 1, 12, 1, 0);
        Duration ttl = Duration.ofSeconds(30);

        articleCreatedTimeRepository.createOrUpdate(articleId, createdAt, ttl);

        LocalDateTime actual = articleCreatedTimeRepository.read(articleId);

        assertThat(actual).isEqualTo(createdAt);
    }

    @Test
    void delete() {
        Long articleId = 1L;

        articleCreatedTimeRepository.delete(articleId);

        LocalDateTime actual = articleCreatedTimeRepository.read(articleId);

        assertThat(actual).isNull();
    }
}
