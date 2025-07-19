package com.example.articleread.cache;

import com.example.dataserializer.DataSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class OptimizedCache {
    private String data;
    private LocalDateTime expired;

    public static OptimizedCache of(Object data, Duration ttl) {
        OptimizedCache cache = new OptimizedCache();
        cache.data = DataSerializer.serialize(data);
        cache.expired = LocalDateTime.now().plus(ttl);
        return cache;
    }

    @JsonIgnore
    public boolean isExpiredData() {
        return LocalDateTime.now().isAfter(expired);
    }

    public <T> T parseData(Class<T> dataType) {
        return DataSerializer.deserialize(data, dataType);
    }
}
