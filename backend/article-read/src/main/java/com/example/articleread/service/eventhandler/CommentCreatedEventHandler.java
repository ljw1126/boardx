package com.example.articleread.service.eventhandler;

import com.example.articleread.repository.ArticleQueryModelRepository;
import com.example.event.Event;
import com.example.event.EventType;
import com.example.event.paylod.CommentCreatedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentCreatedEventHandler implements EventHandler<CommentCreatedEventPayload>{
    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<CommentCreatedEventPayload> event) {
        CommentCreatedEventPayload payload = event.getPayload();

        articleQueryModelRepository.read(payload.getArticleId())
                .ifPresent(articleQueryModel -> {
                    articleQueryModel.updatedBy(payload);
                    articleQueryModelRepository.update(articleQueryModel);
                });
    }

    @Override
    public boolean supports(Event<CommentCreatedEventPayload> event) {
        return EventType.COMMENT_CREATED == event.getType();
    }
}
