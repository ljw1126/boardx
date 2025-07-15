package com.example.articleread.controller;

import com.example.articleread.service.ArticleReadService;
import com.example.articleread.service.response.ArticleReadResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(ArticleReadController.class)
class ArticleReadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleReadService articleReadService;

    @Test
    void read() throws Exception {
        Long articleId = 156358300376981504L;
        LocalDateTime createdAt = LocalDateTime.of(2025, 3, 7, 20, 12, 04);
        ArticleReadResponse response =
                new ArticleReadResponse(articleId, "title0", "content0", 1L, 1L, createdAt, createdAt, 0L, 0L, 0L);


        when(articleReadService.read(articleId))
                .thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/article/{articleId}", articleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articleId").value(articleId));
    }
}
