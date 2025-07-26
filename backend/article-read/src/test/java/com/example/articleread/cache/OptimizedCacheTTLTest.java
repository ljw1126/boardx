package com.example.articleread.cache;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class OptimizedCacheTTLTest {

    @Test
    void of() {
        long ttlSeconds = 10L;

        OptimizedCacheTTL optimizedCacheTTL = OptimizedCacheTTL.of(ttlSeconds);

        assertThat(optimizedCacheTTL.getLogicalTTl()).isEqualTo(Duration.ofSeconds(ttlSeconds));
        assertThat(optimizedCacheTTL.getPhysicalTTl()).isEqualTo(
                Duration.ofSeconds(ttlSeconds).plusSeconds(5)
        );
    }

    @Test
    void physicalTtlGreaterThanLogicalTtl() {
        long ttlSeconds = 10L;

        OptimizedCacheTTL optimizedCacheTTL = OptimizedCacheTTL.of(ttlSeconds);

        assertThat(optimizedCacheTTL.getPhysicalTTl()).isGreaterThan(optimizedCacheTTL.getLogicalTTl());
    }
}
