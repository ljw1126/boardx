package com.example.articleread.service;

import com.example.articleread.EmbeddedRedis;
import com.example.articleread.client.ArticleClient;
import com.example.articleread.client.CommentClient;
import com.example.articleread.client.LikeClient;
import com.example.articleread.client.ViewClient;
import com.example.articleread.config.KafkaConfig;
import com.example.articleread.consumer.ArticleReadEventConsumer;
import com.example.articleread.repository.ArticleIdListRepository;
import com.example.articleread.repository.ArticleQueryModel;
import com.example.articleread.repository.ArticleQueryModelRepository;
import com.example.articleread.repository.BoardArticleCountRepository;
import com.example.articleread.service.response.ArticleReadPageResponse;
import com.example.articleread.service.response.ArticleReadResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
@Import(EmbeddedRedis.class)
class ArticleReadServiceIntegrationTest {

    @Autowired
    private ArticleReadService articleReadService;

    @MockitoBean
    private ArticleClient articleClient;

    @MockitoBean
    private CommentClient commentClient;

    @MockitoBean
    private LikeClient likeClient;

    @MockitoBean
    private ViewClient viewClient;

    // kafka 의존성은 불필요
    @MockitoBean
    private ArticleReadEventConsumer consumer;

    @MockitoBean
    private KafkaConfig kafkaConfig;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ArticleQueryModelRepository articleQueryModelRepository;

    @Autowired
    private ArticleIdListRepository articleIdListRepository;

    @Autowired
    private BoardArticleCountRepository boardArticleCountRepository;

    @BeforeEach
    void setUp() {
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        RedisConnection connection = connectionFactory.getConnection();
        RedisServerCommands redisServerCommands = connection.serverCommands();
        redisServerCommands.flushAll();

        for (long i = 0; i < 30; i++) {
            long articleId = 156358300376981504L + i;
            LocalDateTime createdAt = LocalDateTime.of(2025, 3, 7, 20, 0, 0).plusMinutes(i);

            ArticleQueryModel article = new ArticleQueryModel(
                    articleId,
                    "title" + i,
                    "content" + i,
                    1L,       // boardId
                    i % 5 + 1, // writerId
                    createdAt,
                    createdAt,
                    i,        // commentCount
                    i * 2     // likeCount
            );

            articleQueryModelRepository.create(article, Duration.ofDays(1));
            articleIdListRepository.add(1L, articleId, 100L); // 게시판 ID = 1L 로 고정
        }

        boardArticleCountRepository.save(1L, 30L); // boardId=1에 게시글 총 30개
    }

    @DisplayName("조회되지 않는 게시글의 경우 예외를 던진다")
    @Test
    void readThrowExceptionWhenUnknownArticleId() {
        Long articleId = 99999999999999L;

        when(articleClient.read(articleId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleReadService.read(articleId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("캐싱된 게시글 한건을 조회한다")
    @Test
    void read() {
        Long articleId = 156358300376981504L;

        when(viewClient.count(articleId))
                .thenReturn(1L);

        ArticleReadResponse result= articleReadService.read(articleId);

        assertThat(result).extracting(ArticleReadResponse::getArticleId, ArticleReadResponse::getTitle, ArticleReadResponse::getContent, ArticleReadResponse::getArticleViewCount)
                .containsExactly(articleId, "title0", "content0", 1L);
    }

    @DisplayName("데이터베이스에 없는 경우 직접 조회 요청하여 게시글을 응답한다")
    @Test
    void readByFetch() {
        Long articleId = 777777777777777777L;

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
    }

    /**
     * readAllArticleIds(boardId, page, pageSize)
     * - 1) articleIdListRepository에서 페이징 만큼 articleIds를 조회
     * - 2) (페이징 개수와 다를 경우) 직접 articleClient 호출 후 반환 // 캐싱 x
     */

    /**
     * readAll
     * - articleQueryModelRepository에서 articleIds 를 일괄 조회한다
     * - 변환과정에 없으면 직접 fetch 요청 후 redis 캐싱
     * - ArticleReadResponse 변환하여 리턴
     */

    /**
     * articleCount(boardId) 호출시
     * - 1) boardArticleCountRepository 조회
     * - 2) articleClient 직접 호출 후 boardArticleCountRepository 저장
     */
    @DisplayName("페이징 방식으로 게시글 목록을 조회한다")
    @Test
    void readAll() {
        Long boardId = 1L;
        Long page = 0L;
        Long pageSize = 10L;

        ArticleReadPageResponse articleReadPageResponse = articleReadService.readAll(boardId, page, pageSize);

        assertThat(articleReadPageResponse.getArticles()).hasSize(10);
        assertThat(articleReadPageResponse.getArticleCount()).isEqualTo(30L);
    }

    @DisplayName("페이징 목록 조회시 개시글이 없는 경우 빈 배열을 반환한다")
    @Test
    void readAllWhenInvalidPaging() {
        Long boardId = 1L;
        Long page = 4L; // page default is 1
        Long pageSize = 10L;

        when(articleClient.readAll(boardId, page, pageSize))
                .thenReturn(ArticleClient.ArticlePageResponse.EMPTY);

        ArticleReadPageResponse articleReadPageResponse = articleReadService.readAll(boardId, page, pageSize);

        assertThat(articleReadPageResponse.getArticles()).isEmpty();
        assertThat(articleReadPageResponse.getArticleCount()).isEqualTo(30L);
    }



}
