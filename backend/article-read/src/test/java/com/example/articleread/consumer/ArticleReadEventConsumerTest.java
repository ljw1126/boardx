package com.example.articleread.consumer;

import com.example.articleread.service.ArticleReadService;
import com.example.event.Event;
import com.example.event.EventPayload;
import com.example.event.EventType;
import com.example.event.paylod.ArticleLikedEventPayload;
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
class ArticleReadEventConsumerTest {
    @InjectMocks
    private ArticleReadEventConsumer consumer;

    @Mock
    private ArticleReadService articleReadService;

    @Captor
    ArgumentCaptor<Event<EventPayload>> eventCaptor;

    @Test
    void listener() {
        Long articleLikeId = 1L;
        Long articleId = 1L;
        Long userId = 1L;
        LocalDateTime createdAt = LocalDateTime.now();
        Long articleLikeCount = 2L;
        ArticleLikedEventPayload payload
                = new ArticleLikedEventPayload(articleLikeId, articleId, userId, createdAt, articleLikeCount);

        Event<EventPayload> event = Event.of(articleId, EventType.ARTICLE_LIKED, payload);
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        consumer.listener(event.toJson(), acknowledgment);

        verify(acknowledgment).acknowledge();;
        verify(articleReadService).handleEvent(eventCaptor.capture());

        Event<EventPayload> captured = eventCaptor.getValue();
        assertThat(captured.getEventId()).isEqualTo(articleId);
        assertThat(captured.getType()).isEqualTo(EventType.ARTICLE_LIKED);
        assertThat(captured.getPayload()).isInstanceOf(ArticleLikedEventPayload.class);

    }
}
