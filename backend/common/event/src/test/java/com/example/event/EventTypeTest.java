package com.example.event;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class EventTypeTest {

    @ParameterizedTest
    @MethodSource("provideEventType")
    void from(String type, EventType expected) {
        EventType eventType = EventType.from(type);

        assertThat(eventType).isEqualTo(expected);
    }

    private static Stream<Arguments> provideEventType() {
        return Stream.of(
                Arguments.of("ARTICLE_CREATED", EventType.ARTICLE_CREATED),
                Arguments.of("ARTICLE_UPDATED", EventType.ARTICLE_UPDATED),
                Arguments.of("ARTICLE_DELETED", EventType.ARTICLE_DELETED),
                Arguments.of("COMMENT_CREATED", EventType.COMMENT_CREATED),
                Arguments.of("COMMENT_DELETED", EventType.COMMENT_DELETED),
                Arguments.of("ARTICLE_LIKED", EventType.ARTICLE_LIKED),
                Arguments.of("ARTICLE_UNLIKED", EventType.ARTICLE_UNLIKED),
                Arguments.of("ARTICLE_VIEWED", EventType.ARTICLE_VIEWED)
        );
    }

}
