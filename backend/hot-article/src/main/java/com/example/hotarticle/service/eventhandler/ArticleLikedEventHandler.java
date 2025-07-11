package com.example.hotarticle.service.eventhandler;

import com.example.event.Event;
import com.example.event.EventType;
import com.example.event.paylod.ArticleLikedEventPayload;
import com.example.hotarticle.repository.ArticleLikeCountRepository;
import com.example.hotarticle.utils.TimeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ArticleLikedEventHandler implements EventHandler<ArticleLikedEventPayload>{
    private final ArticleLikeCountRepository articleLikeCountRepository;

    @Override
    public Long findArticleId(Event<ArticleLikedEventPayload> event) {
        return event.getPayload().getArticleId();
    }

    @Override
    public void handle(Event<ArticleLikedEventPayload> event) {
        ArticleLikedEventPayload payload = event.getPayload();
        articleLikeCountRepository.createOrUpdate(
                payload.getArticleId(),
                payload.getArticleLikeCount(),
                TimeCalculator.calculateDurationToMidnight(LocalDateTime.now())
        );
    }

    @Override
    public boolean support(Event<ArticleLikedEventPayload> event) {
        return EventType.ARTICLE_LIKED == event.getType();
    }
}
