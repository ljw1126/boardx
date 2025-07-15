package com.example.articleread.service.eventhandler;

import com.example.articleread.repository.ArticleIdListRepository;
import com.example.articleread.repository.ArticleQueryModel;
import com.example.articleread.repository.ArticleQueryModelRepository;
import com.example.articleread.repository.BoardArticleCountRepository;
import com.example.event.Event;
import com.example.event.EventType;
import com.example.event.paylod.ArticleCreatedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ArticleCreatedEventHandler implements EventHandler<ArticleCreatedEventPayload> {
    private final ArticleIdListRepository articleIdListRepository;
    private final ArticleQueryModelRepository articleQueryModelRepository;
    private final BoardArticleCountRepository boardArticleCountRepository;

    @Override
    public void handle(Event<ArticleCreatedEventPayload> event) {
        ArticleCreatedEventPayload payload = event.getPayload();

        articleQueryModelRepository.create(ArticleQueryModel.from(payload), Duration.ofDays(1));
        articleIdListRepository.add(payload.getBoardId(), payload.getArticleId(), 1000L);
        boardArticleCountRepository.save(payload.getBoardId(), payload.getBoardArticleCount());
    }

    @Override
    public boolean supports(Event<ArticleCreatedEventPayload> event) {
        return EventType.ARTICLE_CREATED == event.getType();
    }
}
