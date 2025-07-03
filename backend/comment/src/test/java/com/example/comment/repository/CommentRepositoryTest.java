package com.example.comment.repository;

import com.example.comment.entity.Comment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest(showSql = false)
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TestEntityManager entityManager;

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
        entityManager.flush();
        entityManager.clear();
    }

    private static Comment of(Long commentId, Long parentCommentId) {
        return Comment.of(commentId, "콘텐츠" + commentId, parentCommentId, 1L, 1L);
    }

    @Test
    void findAll() {
        List<Comment> comments = commentRepository.findAll(1L, 0L, 10L);

        assertThat(comments).hasSize(10);
        assertThat(comments).extracting(Comment::getParentCommentId)
                .containsOnly(1L);
    }

    @Test
    void count() {
        Long count = commentRepository.count(1L, 101L); // 101까지 있어야 페이징에 다음 버튼 출력

        assertThat(count).isEqualTo(100L);
    }

    @Test
    void findAllInfiniteScrollWhenFirst() {
        List<Comment> comments = commentRepository.findAllInfiniteScroll(1L, 10L);

        assertThat(comments).hasSize(10);
        assertThat(comments).extracting(Comment::getParentCommentId)
                .containsOnly(1L);
    }

    @Test
    void findAllInfiniteScrollWhenNext() {
        List<Comment> comments = commentRepository.findAllInfiniteScroll(1L, 1L, 10L, 10L);

        assertThat(comments).hasSize(10);
        assertThat(comments).extracting(Comment::getParentCommentId)
                .containsOnly(11L);
    }

    @Test
    void findAllInfiniteScrollWhenSameParentCommentId() {
        // 5개씩 조회했을 때
        List<Comment> comments = commentRepository.findAllInfiniteScroll(1L, 1L, 5L, 5L);

        assertThat(comments).hasSize(5);
        assertThat(comments).extracting(Comment::getParentCommentId)
                .containsOnly(1L);
    }

    @DisplayName("부모와 자식 댓글이 2개 있는지 확인한다")
    @Test
    void countBy() {
        Long count = commentRepository.countBy(1L, 1L, 2L);

        assertThat(count).isEqualTo(2L);
    }

    @DisplayName("존재하지 않는 parentCommentId로 조회하면 0을 반환한다")
    @Test
    void countByWhenUnknownComment() {
        Long count = commentRepository.countBy(1L, 101L, 2L);

        assertThat(count).isEqualTo(0L);
    }
}
