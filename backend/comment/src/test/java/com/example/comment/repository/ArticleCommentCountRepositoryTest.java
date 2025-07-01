package com.example.comment.repository;

import com.example.comment.entity.ArticleCommentCount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class ArticleCommentCountRepositoryTest {
    @Autowired
    private ArticleCommentCountRepository articleCommentCountRepository;

    @BeforeEach
    void setUp() {
        articleCommentCountRepository.save(new ArticleCommentCount(1L, 10L));
    }

    @Test
    void increaseSuccess() {
        long articleId = 1L;

        int result = articleCommentCountRepository.increase(articleId);
        Long count = articleCommentCountRepository.findById(articleId).map(ArticleCommentCount::getCommentCount).orElseThrow();

        assertThat(result).isEqualTo(1);
        assertThat(count).isEqualTo(11L);
    }

    @Test
    void increaseFail() {
        long articleId = 99L;

        int result = articleCommentCountRepository.increase(articleId);

        assertThat(result).isZero();
    }

    @Test
    void decreaseSuccess() {
        long articleId = 1L;

        int result = articleCommentCountRepository.decrease(articleId);
        Long count = articleCommentCountRepository.findById(articleId).map(ArticleCommentCount::getCommentCount).orElseThrow();

        assertThat(result).isEqualTo(1);
        assertThat(count).isEqualTo(9L);
    }

    @Test
    void decreaseFail() {
        long articleId = 99L;

        int result = articleCommentCountRepository.decrease(articleId);

        assertThat(result).isEqualTo(0);
    }
}
