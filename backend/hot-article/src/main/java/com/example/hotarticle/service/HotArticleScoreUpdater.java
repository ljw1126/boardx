package com.example.hotarticle.service;

import com.example.event.Event;
import com.example.event.EventPayload;
import com.example.hotarticle.repository.ArticleCreatedTimeRepository;
import com.example.hotarticle.repository.HotArticleListRepository;
import com.example.hotarticle.service.eventhandler.EventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class HotArticleScoreUpdater {
    private static final long HOT_ARTICLE_LIMIT = 10;
    private static final Duration HOT_ARTICLE_TTL = Duration.ofDays(10);

    private final ArticleCreatedTimeRepository articleCreatedTimeRepository;
    private final HotArticleScoreCalculator hotArticleScoreCalculator;
    private final HotArticleListRepository hotArticleListRepository;

    public void update(Event<EventPayload> event, EventHandler<EventPayload> eventHandler) {
        Long articleId = eventHandler.findArticleId(event);
        LocalDateTime createdTime = articleCreatedTimeRepository.read(articleId);

        if(isNotCreatedToday(createdTime)) {
            return;
        }

        eventHandler.handle(event);

        long score = hotArticleScoreCalculator.calculate(articleId);
        hotArticleListRepository.add(
                articleId,
                createdTime,
                score,
                HOT_ARTICLE_LIMIT,
                HOT_ARTICLE_TTL
        );
    }

    private boolean isNotCreatedToday(LocalDateTime createdTime) {
        return createdTime == null || !createdTime.toLocalDate().equals(LocalDate.now());
    }
}
