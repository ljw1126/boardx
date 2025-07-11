package com.example.hotarticle.service;

import com.example.event.Event;
import com.example.hotarticle.repository.ArticleCreatedTimeRepository;
import com.example.hotarticle.repository.HotArticleListRepository;
import com.example.hotarticle.service.eventhandler.EventHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotArticleScoreUpdaterTest {

    @InjectMocks
    private HotArticleScoreUpdater hotArticleScoreUpdater;

    @Mock
    private ArticleCreatedTimeRepository articleCreatedTimeRepository;

    @Mock
    private HotArticleScoreCalculator hotArticleScoreCalculator;

    @Mock
    private HotArticleListRepository hotArticleListRepository;

    @Test
    void updateIfArticleNotCreatedToday() {
        Long articleId = 1L;
        Event event = mock(Event.class);
        EventHandler eventHandler = mock(EventHandler.class);

        when(eventHandler.findArticleId(event))
                .thenReturn(articleId);

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        when(articleCreatedTimeRepository.read(articleId))
                .thenReturn(yesterday);

        hotArticleScoreUpdater.update(event, eventHandler);

        verify(eventHandler, never()).handle(event);
        verify(hotArticleListRepository, never())
                .add(anyLong(), any(LocalDateTime.class), anyLong(), anyLong(), any(Duration.class));
    }

    @Test
    void update() {
        Long articleId = 1L;
        Event event = mock(Event.class);
        EventHandler eventHandler = mock(EventHandler.class);

        when(eventHandler.findArticleId(event))
                .thenReturn(articleId);

        LocalDateTime today = LocalDateTime.now();
        when(articleCreatedTimeRepository.read(articleId))
                .thenReturn(today);

        hotArticleScoreUpdater.update(event, eventHandler);

        verify(eventHandler).handle(event);
        verify(hotArticleListRepository, times(1))
                .add(anyLong(), any(LocalDateTime.class), anyLong(), anyLong(), any(Duration.class));
    }

}
