package com.example.view.controller;

import com.example.view.EmbeddedRedis;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(EmbeddedRedis.class)
class ArticleViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        Collection<String> redisKey = redisTemplate.keys("*");
        if (redisKey != null && !redisKey.isEmpty()) {
            redisTemplate.delete(redisKey);
        }
    }

    @Test
    void increaseWhenFirstView() throws Exception {
        assertArticleViewCount(requestIncrease(1L, 1L), 1L);
    }

    @Test
    void increase() throws Exception {
        long articleId = 1L;
        redisTemplate.opsForValue().set(articleViewCountKey(articleId), "100");

        assertArticleViewCount(requestIncrease(articleId, 1L), 101L);
    }

    @Test
    void readWhenNoneExistArticleViewCount() throws Exception {
        assertArticleViewCount(requestRead(1L), 0L);
    }

    @Test
    void read() throws Exception {
        Long articleId = 1L;
        redisTemplate.opsForValue().set(articleViewCountKey(articleId), "100");

        assertArticleViewCount(requestRead(articleId), 100L);
    }

    String articleViewCountKey(Long articleId) {
        return String.format("article::%s::view_count", articleId);
    }

    MvcResult requestIncrease(long articleId, long userId) throws Exception {
        return mockMvc.perform(post("/v1/article-view/article/{articleId}/user/{userId}", articleId, userId))
                .andExpect(status().isOk())
                .andReturn();
    }

    MvcResult requestRead(Long articleId) throws Exception {
        return mockMvc.perform(get("/v1/article_view/article/{articleId}/count", articleId))
                .andExpect(status().isOk())
                .andReturn();
    }

    void assertArticleViewCount(MvcResult mvcResult, long expected) throws UnsupportedEncodingException, JsonProcessingException {
        MockHttpServletResponse response = mvcResult.getResponse();
        String contentAsString = response.getContentAsString();
        Long count = objectMapper.readValue(contentAsString, Long.class);

        assertThat(count).isEqualTo(expected);
    }
}
