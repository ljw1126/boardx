package com.example.articleread.repository;

import com.example.articleread.EmbeddedRedis;
import com.example.articleread.config.KafkaConfig;
import com.example.articleread.consumer.ArticleReadEventConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Import(EmbeddedRedis.class)
@ActiveProfiles("test")
@SpringBootTest
class ArticleQueryModelRepositoryTest {

    @Autowired
    private ArticleQueryModelRepository articleQueryModelRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @MockitoBean
    private KafkaConfig kafkaConfig;

    @MockitoBean
    private ArticleReadEventConsumer consumer;

    @BeforeEach
    void setUp() {
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        RedisConnection connection = connectionFactory.getConnection();
        RedisServerCommands redisServerCommands = connection.serverCommands();
        redisServerCommands.flushAll();

        Long articleId = 156358300376981504L;
        LocalDateTime createdAt = LocalDateTime.of(2025, 3, 7, 20, 12, 04);
        ArticleQueryModel articleQueryModel = new ArticleQueryModel(articleId, "title0", "content0", 1L, 1L, createdAt, createdAt, 0L, 0L);

        articleQueryModelRepository.create(articleQueryModel, Duration.ofDays(1));
    }

    @Test
    void createAndRead() {
        Long articleId = 156358300376981504L;

        Optional<ArticleQueryModel> data = articleQueryModelRepository.read(articleId);

        assertThat(data).isPresent();
        assertThat(data.get().getArticleId()).isEqualTo(articleId);
    }

    @Test
    void readWhenNotExistArticleId() {
        Long articleId = 999999999999999999L;

        Optional<ArticleQueryModel> data = articleQueryModelRepository.read(articleId);

        assertThat(data).isEmpty();
    }

    @Test
    void update() {
        Long articleId = 156358300376981504L;
        LocalDateTime createdAt = LocalDateTime.of(2025, 3, 7, 20, 12, 04);
        ArticleQueryModel updatedArticleQueryModel
                = new ArticleQueryModel(articleId, "제목0", "내용0", 1L, 1L, createdAt, createdAt, 10L, 20L);

        articleQueryModelRepository.update(updatedArticleQueryModel);

        ArticleQueryModel result = articleQueryModelRepository.read(articleId).get();
        assertThat(result.getArticleId()).isEqualTo(articleId);
        assertThat(result.getTitle()).isEqualTo("제목0");
        assertThat(result.getContent()).isEqualTo("내용0");
        assertThat(result.getArticleCommentCount()).isEqualTo(10L);
        assertThat(result.getArticleLikeCount()).isEqualTo(20L);
    }

    @Test
    void delete() {
        Long articleId = 156358300376981504L;

        articleQueryModelRepository.delete(articleId);

        Optional<ArticleQueryModel> result = articleQueryModelRepository.read(articleId);

        assertThat(result).isEmpty();
    }
}
