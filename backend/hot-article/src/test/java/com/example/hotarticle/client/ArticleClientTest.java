package com.example.hotarticle.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;

import java.time.LocalDateTime;

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
    private String articleServiceUrl;

    @Test
    void read() {
        Long articleId = 1L;
        String expectedJson = """
            {
                "articleId": 1,
                "title": "Test Article",
                "createdAt": "2025-07-01T12:00:00"
            }
            """;

        // 응답 설정
        mockServer.expect(requestTo(articleServiceUrl + "/v1/article/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedJson, MediaType.APPLICATION_JSON));

        // 호출
        ArticleClient.ArticleResponse response = articleClient.read(articleId);

        // 검증
        assertThat(response).isNotNull();
        assertThat(response.getArticleId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Article");
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 7, 1, 12, 0));

        mockServer.verify();
    }
}
