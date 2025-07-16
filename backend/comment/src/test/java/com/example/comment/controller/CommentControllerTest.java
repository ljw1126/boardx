package com.example.comment.controller;

import com.example.comment.entity.Comment;
import com.example.comment.repository.CommentRepository;
import com.example.comment.service.request.CommentCreateRequest;
import com.example.comment.service.response.CommentResponse;
import com.example.outboxmessagerelay.OutboxEventPublisher;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentRepository commentRepository;

    @MockitoBean
    private OutboxEventPublisher outboxEventPublisher;

    @BeforeEach
    void setUp() {
        List<Comment> roots = new ArrayList<>();
        for (int g = 0; g < 10; g++) {
            int i = g * 10 + 1;
            roots.add(of((long) i, null));
        }

        List<Comment> childs = new ArrayList<>();
        for (Comment root : roots) {
            Long parent = root.getCommentId();
            for (int i = 1; i <= 9; i++) {
                childs.add(of(parent + i, parent));
            }
        }
        commentRepository.saveAll(roots);
        commentRepository.saveAll(childs);
        commentRepository.flush();
    }

    private static Comment of(Long commentId, Long parentCommentId) {
        return Comment.of(commentId, "콘텐츠" + commentId, parentCommentId, 1L, 1L);
    }

    @DisplayName("신규 생성된 comment는 commentId와 parentCommentId가 같다")
    @Test
    void create() throws Exception {
        CommentCreateRequest request = new CommentCreateRequest(1L, "내용없음", null, 1L);

        MvcResult mvcResult = mockMvc.perform(post("/v1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                ).andExpect(status().isCreated())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        String contentAsString = response.getContentAsString();
        CommentResponse commentResponse = objectMapper.readValue(contentAsString, CommentResponse.class);

        assertThat(commentResponse.getCommentId()).isEqualTo(commentResponse.getParentCommentId());
    }

    @Test
    void read() throws Exception {
        mockMvc.perform(get("/v1/comment/{commentId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").value(1L));
    }

    @Test
    void readAll() throws Exception {
        mockMvc.perform(get("/v1/comment")
                .param("articleId", "1")
                .param("page", "1")
                .param("pageSize", "10")
        ).andExpectAll(status().isOk(),
                jsonPath("$.comments.size()").value(10),
                jsonPath("$.commentCount").value(100) // 101개가 되야 다음 버튼 표출
        );
    }

    @DisplayName("처음 조회시 가장 오래된 순으로 pageSize 만큼 조회한다")
    @Test
    void firstReadAllInfiniteScroll() throws Exception {
        mockMvc.perform(get("/v1/comment/infinite-scroll")
                .param("articleId", "1")
                .param("lastParentCommentId", "")
                .param("lastCommentId", "")
                .param("pageSize", "10")
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.size()").value(10)
        );
    }

    @DisplayName("오름차순 기준으로 두번째 대댓글 그룹 10개를 조회한다")
    @Test
    void secondReadAllInfiniteScroll() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/v1/comment/infinite-scroll")
                        .param("articleId", "1")
                        .param("lastParentCommentId", "1")
                        .param("lastCommentId", "10")
                        .param("pageSize", "10")
                ).andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        String contentAsString = response.getContentAsString();

        List<CommentResponse> commentResponses = objectMapper.readValue(contentAsString, new TypeReference<>() {
        });
        assertThat(commentResponses).hasSize(10);
        assertThat(commentResponses).extracting(CommentResponse::getParentCommentId)
                .containsOnly(11L);
    }

    @DisplayName("부모 댓글을 삭제할 때, 자식 댓글이 있으면 논리 삭제를 한다")
    @Test
    void delete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/comment/{commentId}", 1L))
                .andExpect(status().isNoContent());
    }

}
