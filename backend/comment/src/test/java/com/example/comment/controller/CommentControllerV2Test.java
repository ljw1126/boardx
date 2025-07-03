package com.example.comment.controller;

import com.example.comment.entity.CommentPath;
import com.example.comment.entity.CommentV2;
import com.example.comment.repository.CommentRepositoryV2;
import com.example.comment.service.request.CommentCreateRequestV2;
import com.example.comment.service.response.CommentPageResponseV2;
import com.example.comment.service.response.CommentResponseV2;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
class CommentControllerV2Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentRepositoryV2 commentRepository;

    /*
       샘플 데이터 (1depth : 00000 ~ 00009, 2depth: 각 10개씩, 총 11 * 10 = 110개)
       00000
           0000000000
           0000000001
           0000000002
           0000000003
           0000000004
           0000000005
           0000000006
           0000000007
           0000000008
           0000000009
    */
    @BeforeEach
    void setUp() {
        List<CommentV2> comments = new ArrayList<>();
        rec(0, comments, CommentPath.create(""));

        commentRepository.saveAll(comments);
        commentRepository.flush();
    }

    private void rec(int depth, List<CommentV2> comments, CommentPath path) {
        if(depth == 2) {
            return;
        }

        String descendantsTopPath = null;
        for(int i = 1; i <= 10; i++) {
            CommentPath childPath = path.createChildCommentPath(descendantsTopPath);
            comments.add(of(childPath));

            rec(depth + 1, comments, childPath);

            descendantsTopPath = childPath.getPath();
        }
    }

    private final AtomicLong commentIdGenerator = new AtomicLong(0L);
    private CommentV2 of(CommentPath commentPath) {
        return CommentV2.of(commentIdGenerator.incrementAndGet(), "콘텐츠", 1L, 1L, commentPath);
    }

    @Test
    void create() throws Exception {
        CommentCreateRequestV2 request = new CommentCreateRequestV2(1L, "콘텐츠", "", 1L); // 마지막 path 00009 00009

        mockMvc.perform(post("/v2/comment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value("0000A")); // 0 ~ 9, A-Z, a-z
    }

    @Test
    void read() throws Exception {
        Long commentId = 1L;

        mockMvc.perform(get("/v2/comment/{commentId}", commentId))
                .andExpectAll(status().isOk(),
                        jsonPath("$.commentId").value(commentId),
                        jsonPath("$.path").value("00000"));
    }

    @Test
    void readAll() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/v2/comment")
                        .param("articleId", "1")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        String contentAsString = response.getContentAsString();
        CommentPageResponseV2 comments = objectMapper.readValue(contentAsString, new TypeReference<>(){});

        assertThat(comments.getCommentCount()).isEqualTo(101L);
        assertThat(comments.getComments()).extracting(CommentResponseV2::getPath)
                .allMatch(path -> path.startsWith("00000")); // 00000 ~ 00000 00009
    }

    @Test
    void readAllInfiniteScrollWhenFirst() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/v2/comment/infinite-scroll")
                        .param("articleId", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        String contentAsString = response.getContentAsString();
        List<CommentResponseV2> comments = objectMapper.readValue(contentAsString, new TypeReference<>(){});

        assertThat(comments).hasSize(10);
        assertThat(comments).extracting(CommentResponseV2::getPath)
                .allMatch(path -> path.startsWith("00000")); // 00000 ~ 00000 00009
    }

    @Test
    void readAllInfiniteScrollWhenSecond() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/v2/comment/infinite-scroll")
                        .param("articleId", "1")
                        .param("lastPath", "0000000009") // 테스트 편의 위해
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        String contentAsString = response.getContentAsString();
        List<CommentResponseV2> comments = objectMapper.readValue(contentAsString, new TypeReference<>(){});

        assertThat(comments).hasSize(10);
        assertThat(comments).extracting(CommentResponseV2::getPath)
                .allMatch(path -> path.startsWith("00001")); // 00001 ~ 00001 00009
    }

    @Test
    void delete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/v2/comment/{commentId}", 1L))
                .andExpect(status().isNoContent());

        Optional<CommentV2> comment = commentRepository.findById(1L);

        assertThat(comment).isPresent();
        assertThat(comment.get()).extracting(CommentV2::getDeleted)
                .isEqualTo(true);
    }
}
