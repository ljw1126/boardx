package com.example.hotarticle.service.eventhandler;

import com.example.event.Event;
import com.example.event.EventType;
import com.example.event.paylod.ArticleViewedEventPayload;
import com.example.hotarticle.repository.ArticleViewCountRepository;
import com.example.hotarticle.utils.TimeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ArticleViewedEventHandler implements EventHandler<ArticleViewedEventPayload> {
    private final ArticleViewCountRepository articleViewCountRepository;

    @Override
    public Long findArticleId(Event<ArticleViewedEventPayload> event) {
        return event.getPayload().getArticleId();
    }

    @Override
    public void handle(Event<ArticleViewedEventPayload> event) {
        ArticleViewedEventPayload payload = event.getPayload();
        articleViewCountRepository.createOrUpdate(
            payload.getArticleId(),
            payload.getArticleViewCount(),
            TimeCalculator.calculateDurationToMidnight(LocalDateTime.now())
        );
    }

    @Override
    public boolean support(Event<ArticleViewedEventPayload> event) {
        return EventType.ARTICLE_VIEWED == event.getType();
    }
}
