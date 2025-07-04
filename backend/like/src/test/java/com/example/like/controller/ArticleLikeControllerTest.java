package com.example.like.controller;

import com.example.like.entity.ArticleLike;
import com.example.like.entity.ArticleLikeCount;
import com.example.like.repository.ArticleLikeCountRepository;
import com.example.like.repository.ArticleLikeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
class ArticleLikeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ArticleLikeRepository articleLikeRepository;

    @Autowired
    private ArticleLikeCountRepository articleLikeCountRepository;

    @BeforeEach
    void setUp() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status -> {
            articleLikeRepository.save(ArticleLike.of(1L, 1L, 1L));
            articleLikeCountRepository.save(ArticleLikeCount.of(1L, 1L));
        });
    }

    @Test
    void read() throws Exception {
        mockMvc.perform(get("/v1/article-like/article/{articleId}/user/{userId}", 1L, 1L))
                .andExpectAll(status().isOk(),
                        jsonPath("$.articleId").value(1L),
                        jsonPath("$.userId").value(1L)
                );
    }

    @Test
    void like() throws Exception {
        mockMvc.perform(post("/v1/article-like/article/{articleId}/user/{userId}", 1L, 2L))
                .andExpect(status().isCreated());

        ArticleLikeCount articleLikeCount = articleLikeCountRepository.findById(1L).get();
        assertThat(articleLikeCount.getLikeCount()).isEqualTo(2L);
    }

    @Test
    void unlike() throws Exception {
        mockMvc.perform(delete("/v1/article-like/article/{articleId}/user/{userId}", 1L, 1L))
                .andExpect(status().isNoContent());

        ArticleLikeCount articleLikeCount = articleLikeCountRepository.findById(1L).get();
        assertThat(articleLikeCount.getLikeCount()).isEqualTo(0L);
    }

    @Test
    void count() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/v1/article-like/article/{articleId}/count", 1L))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        String contentAsString = response.getContentAsString();
        Long result = objectMapper.readValue(contentAsString, Long.class);

        assertThat(result).isEqualTo(1L);
    }
}
