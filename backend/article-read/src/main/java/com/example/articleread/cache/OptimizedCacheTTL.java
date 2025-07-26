package com.example.articleread.cache;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OptimizedCacheTTL {
    private Duration logicalTTl;
    private Duration physicalTTl;

    public static final long PHYSICAL_TTL_DELAY_SECONDS = 5;

    // logical ttl < physical ttl
    public static OptimizedCacheTTL of(long ttlSeconds) {
        OptimizedCacheTTL optimizedCacheTTL = new OptimizedCacheTTL();
        optimizedCacheTTL.logicalTTl = Duration.ofSeconds(ttlSeconds);
        optimizedCacheTTL.physicalTTl = optimizedCacheTTL.logicalTTl.plusSeconds(PHYSICAL_TTL_DELAY_SECONDS);
        return optimizedCacheTTL;
    }
}
