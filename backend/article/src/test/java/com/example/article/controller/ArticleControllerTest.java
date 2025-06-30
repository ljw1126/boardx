package com.example.article.controller;

import com.example.article.entity.Article;
import com.example.article.repository.ArticleRepository;
import com.example.article.service.request.ArticleCreateRequest;
import com.example.article.service.request.ArticleUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
class ArticleControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArticleRepository articleRepository;

    @BeforeEach
    void setUp() {
        List<Article> articles = new ArrayList<>();
        for(int i = 1; i <= 100; i++) {
            articles.add(create((long) i));
        }

        articleRepository.saveAll(articles);
        articleRepository.flush();;
    }

    private static Article create(Long articleId) {
        return Article.create(articleId,
                "제목" + articleId,
                "콘텐츠" + articleId,
                1L,
                1L
        );
    }

    @Test
    void createTest() throws Exception {
        ArticleCreateRequest request = new ArticleCreateRequest("제목", "콘텐츠", 1L, 1L);

        mockMvc.perform(post("/v1/article")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpectAll(
                        jsonPath("$.articleId").isNotEmpty(),
                        jsonPath("$.title").value("제목"),
                        jsonPath("$.content").value("콘텐츠"),
                        jsonPath("$.writerId").value(1L),
                        jsonPath("$.boardId").value(1L),
                        jsonPath("$.createdAt").isNotEmpty(),
                        jsonPath("$.modifiedAt").isNotEmpty()
                );
    }

    @Test
    void readTest() throws Exception {
        long articleId = 1L;

        mockMvc.perform(get("/v1/article/{articleId}", articleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articleId").value(articleId));
    }

    @Test
    void readAllTest() throws Exception {
        String boardId = "1";
        String page = "2";
        String pageSize = "5"; // 51개가 있어야 페이징 버튼 10개 출력

        mockMvc.perform(get("/v1/article")
                .param("boardId", boardId)
                .param("page", page)
                .param("pageSize", pageSize)
        ).andExpect(status().isOk())
        .andExpectAll(
                jsonPath("$.articles.size()").value(5),
                jsonPath("$.count").value(51));
    }

    @Test
    void updateTest() throws Exception {
        long articleId = 1L;
        String title = "수정된 제목";
        String content = "수정된 콘텐츠";
        ArticleUpdateRequest request = new ArticleUpdateRequest(title, content);

        mockMvc.perform(put("/v1/article/{articleId}", articleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.articleId").value(articleId),
                        jsonPath("$.title").value(title),
                        jsonPath("$.content").value(content),
                        jsonPath("$.createdAt").isNotEmpty(),
                        jsonPath("$.modifiedAt").isNotEmpty()
                );
    }

    @Test
    void deleteTest() throws Exception {
        long articleId = 1L;
        mockMvc.perform(delete("/v1/article/{articleId}", articleId))
                .andExpect(status().isNoContent());

        boolean exists = articleRepository.findById(articleId).isPresent();
        assertThat(exists).isFalse();
    }
}
