package com.example.articleread.service.eventhandler;

import com.example.event.Event;
import com.example.event.EventPayload;

public interface EventHandler<T extends EventPayload> {
    void handle(Event<T> event);
    boolean supports(Event<T> event);
}
