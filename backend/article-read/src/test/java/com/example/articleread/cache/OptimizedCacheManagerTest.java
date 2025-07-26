package com.example.articleread.cache;

import com.example.dataserializer.DataSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OptimizedCacheManagerTest {

    @InjectMocks
    private OptimizedCacheManager optimizedCacheManager;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private OptimizedCacheLockProvider optimizedCacheLockProvider;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @DisplayName("캐시 데이터가 없으면 원본 데이터 요청 + 캐싱 후 반환한다")
    @Test
    void process() throws Throwable {
        String type = "testType";
        long ttlSeconds = 10L;
        Object[] args = new Object[] { 123L }; // ViewCount.count(..) 파라미터
        Class<Long> returnType = Long.class; // ViewCount.count return type
        OptimizedCacheOriginDataSupplier<Long> supplier = () -> 100L; // ViewClient.count

        String cachedData = null;
        when(valueOperations.get("testType::123"))
                .thenReturn(cachedData);

        Object result = optimizedCacheManager.process(type, ttlSeconds, args, returnType, supplier);

        assertThat(result).isEqualTo(supplier.get());
        verify(valueOperations, times(1))
                .set(eq("testType::123"), anyString(), any(Duration.class));
    }

    @DisplayName("유효하지 않은 캐시 데이터라면 원본 데이터 요청 + 캐싱 후 반환한다")
    @Test
    void process2() throws Throwable {
        String type = "testType";
        long ttlSeconds = 10L;
        Object[] args = new Object[] { 123L };
        Class<Long> returnType = Long.class;
        OptimizedCacheOriginDataSupplier<Long> supplier = () -> 100L;

        String cachedData = "{::invalid";
        when(valueOperations.get("testType::123"))
                .thenReturn(cachedData);

        Object result = optimizedCacheManager.process(type, ttlSeconds, args, returnType, supplier);

        assertThat(result).isEqualTo(supplier.get());
        verify(valueOperations, times(1))
                .set(eq("testType::123"), anyString(), any(Duration.class));
    }

    /*
        참고. OptimizedCache 역직렬화 javabeans naming issue
        https://medium.com/@baejae/boolean%EC%97%90-is-%EB%B6%99%EC%9D%B4%EC%A7%80-%EB%A7%88%EC%84%B8%EC%9A%94-7b717246d942
    */
    @DisplayName("논리적 ttl이 만료되지 않은 데이터라면 캐시 데이터를 반환한다")
    @Test
    void process3() throws Throwable{
        String type = "testType";
        long ttlSeconds = 10L;
        Object[] args = new Object[] { 123L };
        Class<Long> returnType = Long.class;
        OptimizedCacheOriginDataSupplier<Long> supplier = () -> 100L;

        OptimizedCache optimizedCache = OptimizedCache.of("100", Duration.ofSeconds(ttlSeconds));
        when(valueOperations.get("testType::123"))
                .thenReturn(DataSerializer.serialize(optimizedCache));

        Object result = optimizedCacheManager.process(type, ttlSeconds, args, returnType, supplier);

        assertThat(result).isEqualTo(supplier.get());
        verify(valueOperations, never())
                .set(anyString(), anyString(), any(Duration.class));
    }

    @DisplayName("논리적 ttl이 만료된 데이터이고 락 획득 성공시 캐시를 리프레쉬한 후 반환한다")
    @Test
    void process4() throws Throwable {
        String type = "testType";
        long ttlSeconds = 10L;
        Object[] args = new Object[] { 123L };
        Class<Long> returnType = Long.class;
        OptimizedCacheOriginDataSupplier<Long> supplier = () -> 100L;

        OptimizedCache optimizedCache = OptimizedCache.of("100", Duration.ofSeconds(-1));
        when(valueOperations.get("testType::123"))
                .thenReturn(DataSerializer.serialize(optimizedCache));

        when(optimizedCacheLockProvider.lock(anyString()))
                .thenReturn(true);

        Object result = optimizedCacheManager.process(type, ttlSeconds, args, returnType, supplier);

        assertThat(result).isEqualTo(supplier.get());
        verify(valueOperations, times(1))
                .set(eq("testType::123"), anyString(), any(Duration.class));
        verify(optimizedCacheLockProvider, times(1)).unlock(anyString());
    }

    @DisplayName("논리적 ttl이 만료된 데이터이고 락 획득 실패시 캐시 데이터 반환한다 (물리 ttl이 살아있는 상태)")
    @Test
    void process5() throws Throwable {
        String type = "testType";
        long ttlSeconds = 10L;
        Object[] args = new Object[] { 123L };
        Class<Long> returnType = Long.class;
        OptimizedCacheOriginDataSupplier<Long> supplier = () -> 100L;

        OptimizedCache optimizedCache = OptimizedCache.of("100", Duration.ofSeconds(-1));
        when(valueOperations.get("testType::123"))
                .thenReturn(DataSerializer.serialize(optimizedCache));

        when(optimizedCacheLockProvider.lock(anyString()))
                .thenReturn(false);

        Object result = optimizedCacheManager.process(type, ttlSeconds, args, returnType, supplier);

        assertThat(result).isEqualTo(supplier.get());
        verify(optimizedCacheLockProvider, never()).unlock(anyString());
    }
}
