package com.example.hotarticle.service.eventhandler;

import com.example.event.Event;
import com.example.event.EventType;
import com.example.event.paylod.CommentDeletedEventPayload;
import com.example.hotarticle.repository.ArticleCommentCountRepository;
import com.example.hotarticle.utils.TimeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CommentDeletedEventHandler implements EventHandler<CommentDeletedEventPayload>{
    private final ArticleCommentCountRepository articleCommentCountRepository;

    @Override
    public Long findArticleId(Event<CommentDeletedEventPayload> event) {
        return event.getPayload().getArticleId();
    }

    @Override
    public void handle(Event<CommentDeletedEventPayload> event) {
        CommentDeletedEventPayload payload = event.getPayload();
        articleCommentCountRepository.createOrUpdate(
                payload.getArticleId(),
                payload.getArticleCommentCount(),
                TimeCalculator.calculateDurationToMidnight(LocalDateTime.now())
        );
    }

    @Override
    public boolean support(Event<CommentDeletedEventPayload> event) {
        return EventType.COMMENT_DELETED == event.getType();
    }
}
