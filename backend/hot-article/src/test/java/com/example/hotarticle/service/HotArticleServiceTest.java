package com.example.hotarticle.service;

import com.example.event.Event;
import com.example.event.EventPayload;
import com.example.event.EventType;
import com.example.event.paylod.ArticleCreatedEventPayload;
import com.example.event.paylod.ArticleLikedEventPayload;
import com.example.hotarticle.client.ArticleClient;
import com.example.hotarticle.repository.HotArticleListRepository;
import com.example.hotarticle.service.eventhandler.EventHandler;
import com.example.hotarticle.service.response.HotArticleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotArticleServiceTest {

    private HotArticleService hotArticleService;

    @Mock
    private ArticleClient articleClient;

    @Mock
    private EventHandler<EventPayload> eventHandler;

    @Mock
    private HotArticleScoreUpdater hotArticleScoreUpdater;

    @Mock
    private HotArticleListRepository hotArticleListRepository;

    @BeforeEach
    void setUp() {
        this.hotArticleService = new HotArticleService(articleClient,
                List.of(eventHandler),
                hotArticleScoreUpdater,
                hotArticleListRepository);
    }

    @Test
    void handleEventWhenUnknownEvent() {
        hotArticleService.handleEvent(null);

        verify(eventHandler, never()).handle(any());
        verify(hotArticleScoreUpdater, never()).update(any(), any());
    }

    @Test
    void handleEventWhenArticleCreatedEvent() {
        // given
        Event<EventPayload> event = Event.of(1L, EventType.ARTICLE_CREATED, new ArticleCreatedEventPayload());

        when(eventHandler.support(event)).thenReturn(true);

        // when
        hotArticleService.handleEvent(event);

        // then
        verify(eventHandler).handle(event);
        verifyNoInteractions(hotArticleScoreUpdater);
    }

    @Test
    void handleEventWhenArticleLikedEvent() {
        Event<EventPayload> event = Event.of(1L, EventType.ARTICLE_LIKED, new ArticleLikedEventPayload());

        when(eventHandler.support(event)).thenReturn(true);

        // when
        hotArticleService.handleEvent(event);

        // then
        verify(eventHandler, never()).handle(event);
        verify(hotArticleScoreUpdater, times(1)).update(any(), any());
    }

    @Test
    void readAll() {
        String dateStr = "20250701";
        List<Long> articleId = List.of(1L, 2L);

        ArticleClient.ArticleResponse articleResponse1 = new ArticleClient.ArticleResponse(1L, "제목1", LocalDateTime.of(2025, 6, 30, 1, 0, 0));
        ArticleClient.ArticleResponse articleResponse2 = new ArticleClient.ArticleResponse(2L, "제목2", LocalDateTime.of(2025, 6, 30, 2, 0, 0));

        when(hotArticleListRepository.readAll(dateStr)).thenReturn(articleId);
        when(articleClient.read(1L)).thenReturn(articleResponse1);
        when(articleClient.read(2L)).thenReturn(articleResponse2);

        List<HotArticleResponse> result = hotArticleService.readAll(dateStr);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(HotArticleResponse::getArticleId)
                .containsExactly(1L, 2L);

    }

}
