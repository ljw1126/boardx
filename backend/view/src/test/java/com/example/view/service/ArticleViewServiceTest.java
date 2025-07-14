package com.example.view.service;

import com.example.outboxmessagerelay.OutboxEventPublisher;
import com.example.view.EmbeddedRedis;
import com.example.view.entity.ArticleViewCount;
import com.example.view.repository.ArticleViewCountBackUpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Import(EmbeddedRedis.class)
class ArticleViewServiceTest {
    @Autowired
    private ArticleViewService articleViewService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ArticleViewCountBackUpRepository articleViewCountBackUpRepository;

    @MockitoBean
    private OutboxEventPublisher outboxEventPublisher;

    @BeforeEach
    void setUp() {
        try(RedisConnection connection = stringRedisTemplate.getConnectionFactory().getConnection()) {
            connection.serverCommands().flushAll();
        }
    }

    String articleViewCountKey(Long articleId) {
        return String.format("article::%s::view_count", articleId);
    }

    String articleViewDistributedLockKey(Long articleId, Long userId) {
        return String.format("view::article::%s::user::%s::lock", articleId, userId);
    }

    @Test
    void increaseWithoutLock() {
        stringRedisTemplate.opsForValue().set(articleViewCountKey(1L), "99");

        Long result = articleViewService.increase(1L, 1L);

        assertThat(result).isEqualTo(100L);
    }

    @Test
    void increaseWithLock() {
        stringRedisTemplate.opsForValue().set(articleViewCountKey(1L), "1");
        stringRedisTemplate.opsForValue().set(articleViewDistributedLockKey(1L, 1L), "");

        Long result = articleViewService.increase(1L, 1L);

        assertThat(result).isEqualTo(1L);
    }

    @Test
    void increaseWithBackUp() {
        long articleId = 1L;
        stringRedisTemplate.opsForValue().set(articleViewCountKey(articleId), "999");

        Long result = articleViewService.increase(articleId, 1L);
        ArticleViewCount articleViewCount = articleViewCountBackUpRepository.findById(articleId).get();

        assertThat(result).isEqualTo(1000L);
        assertThat(articleViewCount.getViewCount()).isEqualTo(1000L);
    }

    @DisplayName("articleId의 조회수를 캐시에서 읽어온다")
    @Test
    void count() {
        long articleId = 1L;
        stringRedisTemplate.opsForValue().set(articleViewCountKey(articleId), "777");

        Long count = articleViewService.count(articleId);

        assertThat(count).isEqualTo(777L);
    }
}
