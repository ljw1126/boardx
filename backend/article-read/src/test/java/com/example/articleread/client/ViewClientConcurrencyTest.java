package com.example.articleread.client;

import com.example.articleread.EmbeddedRedis;
import com.example.articleread.config.KafkaConfig;
import com.example.articleread.consumer.ArticleReadEventConsumer;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Import(EmbeddedRedis.class)
@ActiveProfiles("test")
@SpringBootTest
class ViewClientConcurrencyTest {

    @Autowired
    private ViewClient viewClient;

    private static MockWebServer mockWebServer;

    @MockitoBean
    private KafkaConfig kafkaConfig;

    @MockitoBean
    private ArticleReadEventConsumer consumer;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        registry.add("endpoints.board-view-service.url", () -> mockWebServer.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        if(mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @DisplayName("RestClient 호출을 한번만 한다")
    @Test
    void count() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("123"));

        Long articleId = 1L;
        // 1st call: 실제 HTTP 요청 후 캐싱
        viewClient.count(articleId);

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CountDownLatch countDownLatch = new CountDownLatch(5);
        for(int i = 0; i < 5; i++) {
            executorService.submit(() -> {
                viewClient.count(articleId);
                countDownLatch.countDown();
            });
        }

        countDownLatch.await();
        executorService.shutdown();

        // 실제 HTTP 요청은 1번만 발생했는지 확인
        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
    }
}
