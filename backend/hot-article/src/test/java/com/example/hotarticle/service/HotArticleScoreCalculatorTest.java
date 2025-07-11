package com.example.hotarticle.service;

import com.example.hotarticle.repository.ArticleCommentCountRepository;
import com.example.hotarticle.repository.ArticleLikeCountRepository;
import com.example.hotarticle.repository.ArticleViewCountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.random.RandomGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotArticleScoreCalculatorTest {

    @InjectMocks
    private HotArticleScoreCalculator hotArticleScoreCalculator;

    @Mock
    private ArticleLikeCountRepository articleLikeCountRepository;

    @Mock
    private ArticleViewCountRepository articleViewCountRepository;

    @Mock
    private ArticleCommentCountRepository articleCommentCountRepository;

    @Test
    void calculate() {
        Long articleId = 1L;
        Long likeCount = getRandomCount();
        Long viewCount = getRandomCount();
        Long commentCount = getRandomCount();

        when(articleLikeCountRepository.read(articleId))
                .thenReturn(likeCount);
        when(articleViewCountRepository.read(articleId))
                .thenReturn(viewCount);
        when(articleCommentCountRepository.read(articleId))
                .thenReturn(commentCount);

        long score = hotArticleScoreCalculator.calculate(articleId);

        assertThat(score).isEqualTo(3 * likeCount + 2 * commentCount + 1 * viewCount);
    }

    private static long getRandomCount() {
        return RandomGenerator.getDefault().nextLong(100L);
    }
}
