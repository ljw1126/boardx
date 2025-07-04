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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

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
    void likeWithoutRetry() throws InterruptedException {
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 2; i <= threadCount + 1; i++) {
            long userId = i;
            tasks.add(() -> {
                articleLikeService.like(1L, userId);
                return null;
            });
        }

        long start = System.currentTimeMillis();

        List<Future<Void>> futures = executorService.invokeAll(tasks);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger failure = new AtomicInteger();

        for (Future<Void> f : futures) {
            try {
                f.get();
                success.incrementAndGet();
            } catch (ExecutionException e) {
                failure.incrementAndGet();
            }
        }

        long end = System.currentTimeMillis();

        System.out.println("success : " + success.get() + ", failure : " + failure.get());
        System.out.println((end - start) + "ms");

        ArticleLikeCount articleLikeCount = articleLikeCountRepository.findById(1L).get();
        System.out.println(articleLikeCount.getLikeCount());
    }
}
