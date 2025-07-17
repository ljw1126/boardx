package com.example.articleread.controller;

import com.example.articleread.service.ArticleReadService;
import com.example.articleread.service.response.ArticleReadPageResponse;
import com.example.articleread.service.response.ArticleReadResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

        mockMvc.perform(get("/v1/article/{articleId}", articleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articleId").value(articleId));
    }

    @Test
    void readAllByPaging() throws Exception {
        Long boardId = 1L;
        Long page = 1L;
        Long pageSize = 10L;

        ArticleReadPageResponse response = new ArticleReadPageResponse(Collections.emptyList(), 0L);

        when(articleReadService.readAll(boardId, page, pageSize))
                .thenReturn(response);

        mockMvc.perform(get("/v1/article?boardId={boardId}&page={page}&pageSize={pageSize}", boardId, page, pageSize))
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.articles.size()").value(0L),
                        jsonPath("$.articleCount").value(0L)
                );
    }


}
