package com.example.hotarticle.consumer;

import com.example.event.Event;
import com.example.event.EventPayload;
import com.example.event.EventType;
import com.example.event.paylod.ArticleCreatedEventPayload;
import com.example.hotarticle.service.HotArticleService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HotArticleEventConsumerTest {

    @InjectMocks
    private HotArticleEventConsumer consumer;

    @Mock
    private HotArticleService hotArticleService;

    @Captor
    ArgumentCaptor<Event<EventPayload>> eventCaptor;

    @Test
    void listener() throws JsonProcessingException {
        Long articleId = 1L;
        LocalDateTime now = LocalDateTime.now();
        ArticleCreatedEventPayload payload
                = new ArticleCreatedEventPayload(articleId, "title", "content", 1L, 1L, now, now, 0L);

        Event<EventPayload> event = Event.of(articleId, EventType.ARTICLE_CREATED, payload);
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        consumer.listener(event.toJson(), acknowledgment);

        verify(acknowledgment).acknowledge();
        verify(hotArticleService).handleEvent(eventCaptor.capture());
        Event<EventPayload> captured = eventCaptor.getValue();
        assertThat(captured.getEventId()).isEqualTo(articleId);
        assertThat(captured.getType()).isEqualTo(EventType.ARTICLE_CREATED);
        assertThat(captured.getPayload()).isInstanceOf(ArticleCreatedEventPayload.class);
    }
}
