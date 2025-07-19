package com.example.articleread.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/*
reference
- https://www.youtube.com/watch?v=-ChpDCIjyh0
- https://jojoldu.tistory.com/341
*/
@ActiveProfiles("test")
@RestClientTest(ArticleClient.class)
class ArticleClientTest {
    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ArticleClient articleClient;

    @Value("${endpoints.board-article-service.url}")
    private String baseUrl;

    @DisplayName("게시글 한 건을 조회한다")
    @Test
    void read() {
        Long articleId = 1L;
        String expectedJson = """
            {
                "articleId": 1,
                "title": "Test Article",
                "content": "Test Content",
                "boardId": 1,
                "writerId": 1,
                "createdAt": "2025-07-01T12:00:00",
                "modifiedAt": "2025-07-01T12:00:00"
            }
            """;

        mockServer.expect(requestTo(baseUrl + "/v1/article/" + articleId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedJson, MediaType.APPLICATION_JSON));

        Optional<ArticleClient.ArticleResponse> response = articleClient.read(articleId);

        assertThat(response.isPresent()).isTrue();

        ArticleClient.ArticleResponse articleResponse = response.get();
        assertThat(articleResponse.getArticleId()).isEqualTo(articleId);
        assertThat(articleResponse.getTitle()).isEqualTo("Test Article");
    }

    @DisplayName("게시판(boardId)의 게시글 수를 조회한다")
    @Test
    void count() {
        Long boardId = 1L;
        String expectedJson = "1000";

        mockServer.expect(requestTo(baseUrl + "/v1/article/board/" + boardId + "/count"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedJson, MediaType.APPLICATION_JSON));

        Long count = articleClient.count(boardId);

        assertThat(count).isEqualTo(1000L);
    }

    @Test
    void readAll() {
        Long boardId = 1L;
        Long page = 1L;
        Long pageSize = 10L;

        String expectedJson = """
        {
          "articles": [],
          "count": 0
        }
        """;

        mockServer.expect(requestTo(baseUrl + "/v1/article?boardId=%s&page=%s&pageSize=%s".formatted(boardId, page, pageSize)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedJson, MediaType.APPLICATION_JSON));

        ArticleClient.ArticlePageResponse response = articleClient.readAll(boardId, page, pageSize);

        assertThat(response.getArticles()).isEmpty();
        assertThat(response.getCount()).isEqualTo(0L);
    }

    @Test
    void readAllInfiniteScroll() {
        Long boardId = 1L;
        Long lastArticleId = 156358300376981504L; // 마지막 게시글 (가장 오래된)
        Long pageSize = 10L;

        String expectedJson = """
        []
        """;

        mockServer.expect(requestTo(baseUrl + "/v1/article/infinite-scroll?boardId=%s&lastArticleId=%s&pageSize=%s".formatted(boardId, lastArticleId, pageSize)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedJson, MediaType.APPLICATION_JSON));

        List<ArticleClient.ArticleResponse> responses = articleClient.readAllInfiniteScroll(boardId, lastArticleId, pageSize);

        assertThat(responses).isEmpty();
    }

}
