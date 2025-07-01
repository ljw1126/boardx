package com.example.article.repository;

import com.example.article.entity.BoardArticleCount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class BoardArticleCountRepositoryTest {
    @Autowired
    private BoardArticleCountRepository boardArticleCountRepository;

    @BeforeEach
    void setUp() {
        boardArticleCountRepository.save(new BoardArticleCount(1L, 10L));
    }

    @Test
    void increase() {
        long boardId = 1L;

        int result = boardArticleCountRepository.increase(boardId);
        Long articleCount = boardArticleCountRepository.findById(boardId).map(BoardArticleCount::getArticleCount).orElseThrow();

        assertThat(result).isEqualTo(1);
        assertThat(articleCount).isEqualTo(11L);
    }

    @Test
    void increaseFail() {
        long boardId = 99L;

        int result = boardArticleCountRepository.increase(boardId);

        assertThat(result).isEqualTo(0);
    }

    @Test
    void decrease() {
        long boardId = 1L;

        int result = boardArticleCountRepository.decrease(boardId);
        Long articleCount = boardArticleCountRepository.findById(boardId).map(BoardArticleCount::getArticleCount).orElseThrow();

        assertThat(result).isEqualTo(1);
        assertThat(articleCount).isEqualTo(9L);
    }

    @Test
    void decreaseFail() {
        long boardId = 99L;
        int result = boardArticleCountRepository.decrease(boardId);

        assertThat(result).isEqualTo(0);
    }
}
