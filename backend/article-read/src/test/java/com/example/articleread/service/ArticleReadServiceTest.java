package com.example.articleread.service;

import com.example.articleread.client.ArticleClient;
import com.example.articleread.client.CommentClient;
import com.example.articleread.client.LikeClient;
import com.example.articleread.client.ViewClient;
import com.example.articleread.repository.ArticleIdListRepository;
import com.example.articleread.repository.ArticleQueryModel;
import com.example.articleread.repository.ArticleQueryModelRepository;
import com.example.articleread.repository.BoardArticleCountRepository;
import com.example.articleread.service.eventhandler.EventHandler;
import com.example.articleread.service.response.ArticleReadResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleReadServiceTest {

    @InjectMocks
    private ArticleReadService articleReadService;

    @Mock
    private ArticleClient articleClient;

    @Mock
    private CommentClient commentClient;

    @Mock
    private LikeClient likeClient;

    @Mock
    private ViewClient viewClient;

    @Mock
    private ArticleIdListRepository articleIdListRepository;

    @Mock
    private ArticleQueryModelRepository articleQueryModelRepository;

    @Mock
    private BoardArticleCountRepository boardArticleCountRepository;

    @Mock
    private List<EventHandler> eventHandlers;

    @Test
    void read() {
        Long articleId = 156358300376981504L;
        LocalDateTime createdAt = LocalDateTime.of(2025, 3, 7, 20, 12, 04);
        ArticleQueryModel articleQueryModel = new ArticleQueryModel(articleId, "title0", "content0", 1L, 1L, createdAt, createdAt, 0L, 0L);

        when(articleQueryModelRepository.read(articleId))
                .thenReturn(Optional.of(articleQueryModel));

        when(viewClient.count(articleId))
                .thenReturn(1L);

        ArticleReadResponse result= articleReadService.read(articleId);

        assertThat(result).extracting(ArticleReadResponse::getArticleId, ArticleReadResponse::getTitle, ArticleReadResponse::getContent, ArticleReadResponse::getArticleViewCount)
                .containsExactly(articleId, "title0", "content0", 1L);
    }

    @DisplayName("조회되지 않는 게시글의 경우 예외를 던진다")
    @Test
    void readThrowExceptionWhenUnknownArticleId() {
        Long articleId = -99L;

        when(articleQueryModelRepository.read(articleId))
                .thenReturn(Optional.empty());

        when(articleClient.read(articleId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleReadService.read(articleId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("데이터베이스에 없는 경우 직접 조회 요청하여 게시글을 응답한다")
    @Test
    void readByFetch() {
        Long articleId = 156358300376981504L;

        when(articleQueryModelRepository.read(articleId))
                .thenReturn(Optional.empty());

        LocalDateTime createdAt = LocalDateTime.of(2025, 3, 7, 20, 12, 04);
        ArticleClient.ArticleResponse articleResponse = new ArticleClient.ArticleResponse(articleId, "title0", "content0", 1L, 1L, createdAt, createdAt);
        when(articleClient.read(articleId))
                .thenReturn(Optional.of(articleResponse));

        when(commentClient.count(articleId))
                .thenReturn(1L);
        when(likeClient.count(articleId))
                .thenReturn(2L);
        when(viewClient.count(articleId))
                .thenReturn(3L);


        ArticleReadResponse result = articleReadService.read(articleId);

        assertThat(result)
                .extracting(ArticleReadResponse::getArticleId, ArticleReadResponse::getArticleCommentCount, ArticleReadResponse::getArticleLikeCount, ArticleReadResponse::getArticleViewCount)
                .containsExactly(articleId, 1L, 2L, 3L);

        verify(articleQueryModelRepository, times(1))
                .create(any(), any());
    }

}
