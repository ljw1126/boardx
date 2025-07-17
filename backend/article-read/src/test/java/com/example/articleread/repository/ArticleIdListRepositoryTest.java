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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// 게시판별로 신규 생성된 게시글의 articleId를 저장/조회
@Import(EmbeddedRedis.class)
@ActiveProfiles("test")
@SpringBootTest
class ArticleIdListRepositoryTest {

    @Autowired
    private ArticleIdListRepository articleIdListRepository;

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
        connection.serverCommands().flushAll();

        Long boardId = 1L;
        for(int i = 1; i <= 10; i++) {
            articleIdListRepository.add(boardId, (long) i, 5L); // 5개로 제한
        }
    }

    @Test
    void add() {
        Long boardId = 1L;

        List<Long> ids = redisTemplate.opsForZSet().range(generateKey(boardId), 0, -1)
                .stream()
                .map(Long::valueOf)
                .toList();

        assertThat(ids).hasSize(5)
                .containsExactly(6L, 7L, 8L, 9L, 10L);
    }

    @Test
    void delete() {
        Long boardId = 1L;

        articleIdListRepository.delete(boardId, 9L);

        List<Long> ids = redisTemplate.opsForZSet().range(generateKey(boardId), 0, -1)
                .stream()
                .map(Long::valueOf)
                .toList();

        assertThat(ids).hasSize(4)
                .containsExactly(6L, 7L, 8L, 10L);
    }

    @Test
    void readAll() {
        Long boardId = 1L;
        Long offset = 0L;
        Long limit = 5L;

        List<Long> articleIds = articleIdListRepository.readAll(boardId, offset, limit);

        assertThat(articleIds).hasSize(5);
        assertThat(articleIds).containsExactly(10L, 9L, 8L, 7L, 6L); // 내림차순 조회
    }

    private String generateKey(Long boardId) {
        return "article-read::board::%s::article-list".formatted(boardId);
    }
}
