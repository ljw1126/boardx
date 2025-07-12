package com.example.hotarticle.consumer;

import com.example.event.Event;
import com.example.event.EventPayload;
import com.example.event.EventType;
import com.example.event.paylod.ArticleCreatedEventPayload;
import com.example.hotarticle.service.HotArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@EnableKafka
@ActiveProfiles("test")
@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = {"listener=PLAINTEXT://localhost:9092"},
        topics = {EventType.Topic.BOARD_ARTICLE, EventType.Topic.BOARD_COMMENT, EventType.Topic.BOARD_LIKE, EventType.Topic.BOARD_VIEW})
class EmbeddedKafkaEventConsumerTest {

    @MockitoBean
    private RedisConfiguration redisConfiguration;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    private HotArticleService hotArticleService;

    @Test
    void listener() throws InterruptedException {
        Long articleId = 10L;
        LocalDateTime now = LocalDateTime.now();
        ArticleCreatedEventPayload payload
                = new ArticleCreatedEventPayload(articleId, "title", "content", 1L, 1L, now, now, 0L);

        Event<EventPayload> event = Event.of(articleId, EventType.ARTICLE_CREATED, payload);
        kafkaTemplate.send(EventType.Topic.BOARD_ARTICLE, event.toJson());
        kafkaTemplate.flush();

        Thread.sleep(1000);

        verify(hotArticleService, atLeastOnce()).handleEvent(any());
    }

}
