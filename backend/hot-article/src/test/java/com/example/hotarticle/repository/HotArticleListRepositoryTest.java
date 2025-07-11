package com.example.hotarticle.repository;

import com.example.hotarticle.EmbeddedRedis;
import com.example.hotarticle.config.KafkaConfig;
import com.example.hotarticle.consumer.HotArticleEventConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Import(EmbeddedRedis.class)
@SpringBootTest
class HotArticleListRepositoryTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private HotArticleListRepository hotArticleListRepository;

    // kafka 로그 제거
    @MockitoBean
    private KafkaConfig kafkaConfig;

    @MockitoBean
    private HotArticleEventConsumer hotArticleEventConsumer;

    @Test
    void add() {
        Long limit = 5L;
        Duration ttl = Duration.ofMinutes(1);
        LocalDateTime now = LocalDateTime.now();

        for(int i = 1; i <= 10; i++) {
            Long articleId = (long) i;
            hotArticleListRepository.add(articleId, now, (long) i, limit, ttl);
        }

        List<Long> result = hotArticleListRepository.readAll(FORMATTER.format(now));

        assertThat(result).hasSize(5)
                .containsExactly(10L, 9L, 8L, 7L, 6L);
    }

    @DisplayName("(article 삭제 이벤트 발생시) hot-article을 삭제한다")
    @Test
    void remove() {
        Long limit = 5L;
        Duration ttl = Duration.ofMinutes(1);
        LocalDateTime now = LocalDateTime.now();

        for(int i = 1; i <= 5; i++) {
            Long articleId = (long) i;
            hotArticleListRepository.add(articleId, now, (long) i, limit, ttl);
        }

        hotArticleListRepository.remove(1L, now);

        List<Long> result = hotArticleListRepository.readAll(FORMATTER.format(now));

        assertThat(result).hasSize(4)
                .containsExactly(5L, 4L, 3L, 2L);
    }
}
