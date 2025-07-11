package com.example.hotarticle.repository;

import com.example.hotarticle.EmbeddedRedis;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Import(EmbeddedRedis.class)
@SpringBootTest
class HotArticleListRepositoryTest {

    @Autowired
    private HotArticleListRepository hotArticleListRepository;

    @Test
    void add() {
        Long limit = 5L;
        Duration ttl = Duration.ofMinutes(1);
        LocalDateTime now = LocalDateTime.now();

        for(int i = 1; i <= 10; i++) {
            Long articleId = (long) i;
            hotArticleListRepository.add(articleId, now, (long) i, limit, ttl);
        }

        List<Long> result = hotArticleListRepository.readAll(now);

        assertThat(result).hasSize(5)
                .containsExactly(10L, 9L, 8L, 7L, 6L);
    }
}
