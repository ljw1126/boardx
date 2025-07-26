package com.example.articleread.cache;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class OptimizedCacheTest {
    @Test
    void parseData() {
        assertParseData("data", 10L);
        assertParseData(3L, 10L);
        assertParseData(3, 10L);
        assertParseData(new TestRecord("hello world"), 10);
    }

    private void assertParseData(Object data, long ttlSeconds) {
        OptimizedCache optimizedCache = OptimizedCache.of(data, Duration.ofSeconds(ttlSeconds));

        Object parseData = optimizedCache.parseData(data.getClass());

        assertThat(parseData).isEqualTo(data);
    }

    record TestRecord(String data) {}

    @Test
    void isExpired() {
        assertThat(OptimizedCache.of("data", Duration.ofDays(-30)).isExpiredData()).isTrue();
        assertThat(OptimizedCache.of("data", Duration.ofDays(30)).isExpiredData()).isFalse();
    }
}
