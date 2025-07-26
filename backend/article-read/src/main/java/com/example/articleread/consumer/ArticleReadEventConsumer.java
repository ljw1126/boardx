package com.example.articleread.consumer;

import com.example.articleread.service.ArticleReadService;
import com.example.event.Event;
import com.example.event.EventPayload;
import com.example.event.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleReadEventConsumer {
    private final ArticleReadService articleReadService;

    @KafkaListener(topics = {
            EventType.Topic.BOARD_ARTICLE,
            EventType.Topic.BOARD_COMMENT,
            EventType.Topic.BOARD_LIKE
    })
    public void listener(String message, Acknowledgment ack) {
        log.info("[ArticleReadEventConsumer.listener] message = {}", message);

        Event<EventPayload> event = Event.fromJson(message);
        if(event != null) {
            articleReadService.handleEvent(event);
        }

        ack.acknowledge();;
    }
}
