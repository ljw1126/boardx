package com.example.hotarticle.service.eventhandler;

import com.example.event.Event;
import com.example.event.EventPayload;

public interface EventHandler<T extends EventPayload> {
    Long findArticleId(Event<T> event);
    void handle(Event<T> event);
    boolean support(Event<T> event);
}
