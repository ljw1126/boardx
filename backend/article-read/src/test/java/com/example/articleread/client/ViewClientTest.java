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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ActiveProfiles("test")
@RestClientTest(ViewClient.class)
class ViewClientTest {
    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ViewClient viewClient;

    @Value("${endpoints.board-view-service.url}")
    private String baseUrl;

    @DisplayName("게시글의 조회수를 조회한다")
    @Test
    void count() {
        Long articleId = 1L;
        String expectedJson = "1000";

        mockServer.expect(requestTo(baseUrl + "/v1/article_view/article/" + articleId + "/count"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedJson, MediaType.APPLICATION_JSON));

        Long count = viewClient.count(articleId);

        assertThat(count).isEqualTo(1000L);
    }
}
