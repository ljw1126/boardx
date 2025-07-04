package com.example.like.service;

import com.example.like.entity.ArticleLikeCount;
import com.example.like.repository.ArticleLikeCountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class ArticleLikeServiceTest {

    @Autowired
    private ArticleLikeService articleLikeService;

    @Autowired
    private ArticleLikeCountRepository articleLikeCountRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status -> {
            articleLikeCountRepository.save(ArticleLikeCount.of(1L, 0L));
        });
    }

    @Test
    void like() throws InterruptedException {
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long start = System.currentTimeMillis();
        for (int i = 2; i <= threadCount + 1; i++) {
            long userId = i;
            executorService.submit(() -> {
                articleLikeService.like(1L, userId);
                latch.countDown();
            });
        }

        latch.await();

        long end = System.currentTimeMillis();
        System.out.println((end - start) + "ms");

        ArticleLikeCount articleLikeCount = articleLikeCountRepository.findById(1L).get();
        assertThat(articleLikeCount.getLikeCount()).isEqualTo(threadCount);
    }
}
