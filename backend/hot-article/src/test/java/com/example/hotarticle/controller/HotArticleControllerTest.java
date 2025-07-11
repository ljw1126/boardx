package com.example.hotarticle.controller;

import com.example.hotarticle.service.HotArticleService;
import com.example.hotarticle.service.response.HotArticleResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(HotArticleController.class)
class HotArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HotArticleService hotArticleService;

    @Test
    void readAll() throws Exception {
        String dateStr = "20250701";
        long articleId = 1L;
        String title = "title";
        LocalDateTime createdAt = LocalDateTime.of(2025, 7, 1, 1, 0, 0);
        HotArticleResponse response = new HotArticleResponse(articleId, title, createdAt);

        Mockito.when(hotArticleService.readAll(dateStr))
                        .thenReturn(List.of(response));

        mockMvc.perform(get("/v1/hot-articles/articles/date/{dateStr}", dateStr))
                .andExpectAll(status().isOk(),
                        jsonPath("$[0].articleId").value(articleId),
                        jsonPath("$[0].title").value(title),
                        jsonPath("$[0].createdAt").value("2025-07-01T01:00:00"));

    }
}
