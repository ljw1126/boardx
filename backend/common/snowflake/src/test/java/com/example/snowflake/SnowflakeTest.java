package com.example.snowflake;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

class SnowflakeTest {
    private final Snowflake snowflake = new Snowflake();

    @DisplayName("중복없이 오름차순으로 원하는 갯수 만큼 유니크한 아이디를 만든다")
    @Test
    void nextIdTest() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Future<List<Long>>> futures = new ArrayList<>();
        int repeatCount = 1000;
        int idCount = 1000;

        for(int i = 0; i < repeatCount; i++) {
            futures.add(executorService.submit(() -> generateIdList(idCount)));
        }

        List<Long> result = new ArrayList<>();
        for(Future<List<Long>> future : futures) {
            List<Long> ids = future.get();
            for(int i = 1; i < ids.size(); i++) {
                assertThat(ids.get(i)).isGreaterThan(ids.get(i - 1));
            }
            result.addAll(ids);
        }

        long expected = repeatCount * idCount;
        long actual = result.stream().distinct().count();
        assertThat(actual).isEqualTo(expected);

        executorService.shutdown();
    }

    private List<Long> generateIdList(int count) {
        List<Long> ids = new ArrayList<>();
        for(int i = 1; i <= count; i++) {
            ids.add(snowflake.nextId());
        }

        return ids;
    }

    @Test
    void performanceTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        int repeatCount = 1000;
        int idCount = 1000;
        CountDownLatch latch = new CountDownLatch(repeatCount);

        long start = System.currentTimeMillis();
        for(int i = 0; i < repeatCount; i++) {
            executorService.submit(() -> {
                generateIdList(idCount);
                latch.countDown();
            });
        }

        latch.await();
        long end = System.currentTimeMillis();

        System.out.printf("times = %s ms%n", end - start);
        executorService.shutdown();
    }
}
