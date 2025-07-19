package com.example.articleread.repository;

import com.example.articleread.EmbeddedRedis;
import com.example.articleread.config.KafkaConfig;
import com.example.articleread.consumer.ArticleReadEventConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

// 게시판 별로 게시글 수를 저장한다
@Import(EmbeddedRedis.class)
@ActiveProfiles("test")
@SpringBootTest
class BoardArticleCountRepositoryTest {

    @Autowired
    private BoardArticleCountRepository boardArticleCountRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @MockitoBean
    private KafkaConfig kafkaConfig;

    @MockitoBean
    private ArticleReadEventConsumer consumer;

    @BeforeEach
    void setUp() {
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        RedisServerCommands redisServerCommands = connectionFactory.getConnection().serverCommands();
        redisServerCommands.flushAll();

        boardArticleCountRepository.save(1L, 1000L);
    }

    @Test
    void save() {
        Long boardId = 1L;
        Long articleCount = 1001L;

        boardArticleCountRepository.save(boardId, articleCount);

        String result = redisTemplate.opsForValue().get(generateKey(boardId));

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("1001");
    }

    @Test
    void read() {
        Long boardId = 1L;

        Long articleCount = boardArticleCountRepository.read(boardId);

        assertThat(articleCount).isNotNull()
                .isEqualTo(1000L);
    }

    @Test
    void readWhenUnknownBoardId() {
        Long boardId = 99L;

        Long articleCount = boardArticleCountRepository.read(boardId);

        assertThat(articleCount).isNull();
    }

    private String generateKey(Long boardId) {
        return "article-read::board-article-count::board::%s".formatted(boardId);
    }
}
