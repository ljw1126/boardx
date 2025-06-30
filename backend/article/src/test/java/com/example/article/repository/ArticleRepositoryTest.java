package com.example.article.repository;

import com.example.article.entity.Article;
import com.example.snowflake.Snowflake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest(showSql = false)
class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @BeforeEach
    void setUp() {
        List<Article> articles = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            articles.add(Article.create((long) i, "title" + i, "content" + i, 1L, 1L));
        }
        articleRepository.saveAll(articles);
    }

    @Test
    void findAll() {
        List<Article> articles = articleRepository.findAll(1L, 20L, 10L);

        assertThat(articles.size()).isEqualTo(10);
    }

    @Test
    void findAllWhenOverRange() {
        List<Article> articles = articleRepository.findAll(1L, 100L, 10L);

        assertThat(articles).isEmpty();
    }

    @Test
    void count() {
        Long count = articleRepository.count(1L, 11L);

        assertThat(count).isEqualTo(11L);
    }

    @Test
    void countWhenOver() {
        Long count = articleRepository.count(1L, 101L);

        assertThat(count).isEqualTo(100);
    }

}
