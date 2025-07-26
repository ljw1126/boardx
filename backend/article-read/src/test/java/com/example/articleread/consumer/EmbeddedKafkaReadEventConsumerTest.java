package com.example.articleread.consumer;

import com.example.articleread.service.ArticleReadService;
import com.example.event.Event;
import com.example.event.EventPayload;
import com.example.event.EventType;
import com.example.event.paylod.ArticleLikedEventPayload;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@EnableKafka
@ActiveProfiles("test")
@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = {"listener=PLAINTEXT://localhost:9092"},
        topics = {EventType.Topic.BOARD_ARTICLE, EventType.Topic.BOARD_COMMENT, EventType.Topic.BOARD_LIKE})
class EmbeddedKafkaReadEventConsumerTest {

    @MockitoBean
    private ArticleReadService articleReadService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void listener() throws InterruptedException {
        Long articleLikeId = 1L;
        Long articleId = 1L;
        Long userId = 1L;
        LocalDateTime createdAt = LocalDateTime.now();
        Long articleLikeCount = 2L;
        ArticleLikedEventPayload payload
                = new ArticleLikedEventPayload(articleLikeId, articleId, userId, createdAt, articleLikeCount);

        Event<EventPayload> event = Event.of(articleId, EventType.ARTICLE_LIKED, payload);
        kafkaTemplate.send(EventType.Topic.BOARD_LIKE, event.toJson());
        kafkaTemplate.flush();

        Thread.sleep(1000);

        verify(articleReadService, times(1))
                .handleEvent(any());
    }
}
