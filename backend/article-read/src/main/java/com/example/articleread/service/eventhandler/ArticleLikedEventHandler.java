package com.example.articleread.service.eventhandler;

import com.example.articleread.repository.ArticleQueryModelRepository;
import com.example.event.Event;
import com.example.event.EventType;
import com.example.event.paylod.ArticleLikedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleLikedEventHandler implements EventHandler<ArticleLikedEventPayload>{
    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<ArticleLikedEventPayload> event) {
        ArticleLikedEventPayload payload = event.getPayload();

        articleQueryModelRepository.read(payload.getArticleId())
                .ifPresent(articleQueryModel -> {
                    articleQueryModel.updatedBy(payload);
                    articleQueryModelRepository.update(articleQueryModel);
                });
    }

    @Override
    public boolean supports(Event<ArticleLikedEventPayload> event) {
        return EventType.ARTICLE_LIKED == event.getType();
    }
}
