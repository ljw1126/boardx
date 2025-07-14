package com.example.article.service;

import com.example.article.entity.Article;
import com.example.article.repository.ArticleRepository;
import com.example.article.repository.BoardArticleCountRepository;
import com.example.article.service.request.ArticleCreateRequest;
import com.example.article.service.request.ArticleUpdateRequest;
import com.example.article.service.response.ArticlePageResponse;
import com.example.article.service.response.ArticleResponse;
import com.example.outboxmessagerelay.OutboxEventPublisher;
import com.example.snowflake.Snowflake;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    private final Snowflake snowflake = new Snowflake();

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private BoardArticleCountRepository boardArticleCountRepository;

    @Mock
    private OutboxEventPublisher outboxEventPublisher;

    @InjectMocks
    private ArticleService articleService;

    @Test
    void create() {
        ArticleCreateRequest request = new ArticleCreateRequest("제목", "콘텐츠", 1L, 1L);
        Article article = create(snowflake.nextId());

        when(articleRepository.save(any()))
                .thenReturn(article);
        when(boardArticleCountRepository.increase(anyLong()))
                .thenReturn(1);

        ArticleResponse result = articleService.create(request);

        assertThat(result).extracting(ArticleResponse::getTitle, ArticleResponse::getContent)
                .containsExactly(article.getTitle(), article.getContent());

        verify(boardArticleCountRepository, never()).save(any());
        verify(outboxEventPublisher, times(1)).publish(any(), any(), anyLong());
    }

    @Test
    void createWhenFirstArticle() {
        ArticleCreateRequest request = new ArticleCreateRequest("제목", "콘텐츠", 1L, 1L);
        Article article = create(snowflake.nextId());

        when(articleRepository.save(any()))
                .thenReturn(article);
        when(boardArticleCountRepository.increase(anyLong()))
                .thenReturn(0);

        ArticleResponse result = articleService.create(request);

        assertThat(result).extracting(ArticleResponse::getTitle, ArticleResponse::getContent)
                .containsExactly(article.getTitle(), article.getContent());

        verify(boardArticleCountRepository, times(1)).save(any());
        verify(outboxEventPublisher, times(1)).publish(any(), any(), anyLong());
    }

    @Test
    void read() {
        Long articleId = snowflake.nextId();

        when(articleRepository.findById(articleId))
                .thenReturn(Optional.of(create(articleId)));

        ArticleResponse result = articleService.read(articleId);

        assertThat(result.getArticleId()).isEqualTo(articleId);
    }

    @Test
    void readWhenNoneExists() {
        Long articleId = snowflake.nextId();

        when(articleRepository.findById(articleId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.read(articleId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void readAll() {
        Long boardId = 1L;
        Long page = 1L;
        Long pageSize = 10L;

        when(articleRepository.findAll(anyLong(), anyLong(), anyLong()))
            .thenReturn(List.of(create(snowflake.nextId()), create(snowflake.nextId()), create(snowflake.nextId())));
        when(articleRepository.count(anyLong(), anyLong()))
                .thenReturn(3L);

        ArticlePageResponse articlePageResponse = articleService.readAll(boardId, page, pageSize);

        assertThat(articlePageResponse.getArticles()).hasSize(3);
        assertThat(articlePageResponse.getCount()).isEqualTo(3L);
    }

    @Test
    void update() {
        Long articleId = snowflake.nextId();
        String title = "수정 제목";
        String content = "수정 콘텐츠";
        ArticleUpdateRequest request = new ArticleUpdateRequest(title, content);

        when(articleRepository.findById(articleId))
                .thenReturn(Optional.of(create(articleId)));

        ArticleResponse result = articleService.update(articleId, request);

        assertThat(result).extracting(ArticleResponse::getArticleId, ArticleResponse::getTitle, ArticleResponse::getContent)
                .containsExactly(articleId, title, content);
        verify(outboxEventPublisher, times(1)).publish(any(), any(), anyLong());
    }

    @Test
    void delete() {
        Long articleId = snowflake.nextId();

        when(articleRepository.findById(articleId))
                .thenReturn(Optional.of(create(articleId)));

        articleService.delete(articleId);

        verify(articleRepository).delete(any());
        verify(boardArticleCountRepository).decrease(anyLong());
        verify(outboxEventPublisher, times(1)).publish(any(), any(), anyLong());
    }

    private static Article create(Long articleId) {
        return Article.create(articleId, "제목", "콘텐츠", 1L, 1L);
    }

    @Test
    void readAllInfiniteScrollWhenLastArticleIdIsNull() {
        Long boardId = 1L;
        Long pageSize = 10L;

        when(articleRepository.readAllInfiniteScroll(boardId, pageSize))
                .thenReturn(List.of(create(boardId)));

        List<ArticleResponse> articleResponses = articleService.readAllInfiniteScroll(boardId, pageSize, null);

        assertThat(articleResponses).hasSize(1);
        verify(articleRepository, times(1)).readAllInfiniteScroll(boardId, pageSize);
    }

    @Test
    void readAllInfiniteScrollWhenLastArticleIdIsNotNull() {
        Long boardId = 1L;
        Long pageSize = 10L;
        Long lastArticleId = 40L;
        when(articleRepository.readAllInfiniteScroll(boardId, pageSize, lastArticleId))
                .thenReturn(List.of(create(boardId)));

        List<ArticleResponse> articleResponses = articleService.readAllInfiniteScroll(boardId, pageSize, lastArticleId);

        assertThat(articleResponses).hasSize(1);
        verify(articleRepository, times(1)).readAllInfiniteScroll(boardId, pageSize, lastArticleId);
    }
}
