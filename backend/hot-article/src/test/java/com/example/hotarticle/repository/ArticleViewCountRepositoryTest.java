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

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Import(EmbeddedRedis.class)
@SpringBootTest
class ArticleViewCountRepositoryTest {

    @Autowired
    private ArticleViewCountRepository articleViewCountRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // kafka 로그 제거
    @MockitoBean
    private KafkaConfig kafkaConfig;

    @MockitoBean
    private HotArticleEventConsumer hotArticleEventConsumer;

    @BeforeEach
    void setUp() {
        RedisConnectionFactory connectionFactory = stringRedisTemplate.getConnectionFactory();
        RedisConnection connection = connectionFactory.getConnection();
        connection.serverCommands().flushAll();

        articleViewCountRepository.createOrUpdate(1L, 100L, Duration.ofSeconds(30));
    }

    @Test
    void read() {
        Long articleId = 1L;

        Long actual = articleViewCountRepository.read(articleId);

        assertThat(actual).isEqualTo(100L);
    }

    @Test
    void create() {
        Long articleId = 2L;
        Long commentCount = 100L;
        Duration ttl = Duration.ofSeconds(30);

        articleViewCountRepository.createOrUpdate(articleId, commentCount, ttl);

        Long actual = articleViewCountRepository.read(articleId);

        assertThat(actual).isEqualTo(100L);
    }

    @Test
    void update() {
        Long articleId = 1L;
        Long commentCount = 200L;
        Duration ttl = Duration.ofSeconds(30);

        articleViewCountRepository.createOrUpdate(articleId, commentCount, ttl);

        Long actual = articleViewCountRepository.read(articleId);

        assertThat(actual).isEqualTo(200L);
    }
}
