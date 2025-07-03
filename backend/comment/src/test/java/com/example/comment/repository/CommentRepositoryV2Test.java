package com.example.comment.repository;

import com.example.comment.entity.CommentPath;
import com.example.comment.entity.CommentV2;
import com.example.snowflake.Snowflake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest(showSql = false)
class CommentRepositoryV2Test {

    private final Snowflake snowflake = new Snowflake();

    @Autowired
    private CommentRepositoryV2 commentRepository;

    @Autowired
    private TestEntityManager entityManager;

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
        entityManager.flush();
        entityManager.clear();
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

    private CommentV2 of(CommentPath commentPath) {
        return CommentV2.of(snowflake.nextId(), "콘텐츠", 1L, 1L, commentPath);
    }

    @Test
    void findByPath() {
        String path = "00000".repeat(2);
        Optional<CommentV2> comment = commentRepository.findByPath(path);

        assertThat(comment).isPresent();
    }

    @Test
    void findByPathWhenNotFound() {
        String path = "zzzzz".repeat(5);
        Optional<CommentV2> comment = commentRepository.findByPath(path);

        assertThat(comment).isEmpty();
    }

    @Test
    void findDescendantsTopPath() {
        String pathPrefix = "00000";
        Optional<String> descendantsTopPath = commentRepository.findDescendantsTopPath(1L, pathPrefix);

        assertThat(descendantsTopPath).isPresent();
        assertThat(descendantsTopPath.get()).isEqualTo("0000000009");
    }

    @Test
    void findDescendantsTopPathWhenNotFound() {
        String pathPrefix = "00009000010"; // 마지막 : 00009 00009
        Optional<String> descendantsTopPath = commentRepository.findDescendantsTopPath(1L, pathPrefix);

        assertThat(descendantsTopPath).isEmpty();
    }

    @Test
    void findAll() {
        List<CommentV2> comments = commentRepository.findAll(1L, 0L, 10L);

        assertThat(comments).hasSize(10);
        assertThat(comments).extracting(CommentV2::getPath)
                .allMatch(path -> path.startsWith("00000"));
    }

    @Test
    void count() {
        Long count = commentRepository.count(1L, 101L);

        assertThat(count).isEqualTo(101L);
    }

    @Test
    void findAllInfiniteScrollWhenFirst() {
        List<CommentV2> comments = commentRepository.findAllInfiniteScroll(1L, 10L);

        assertThat(comments).hasSize(10);
        assertThat(comments).extracting(CommentV2::getPath)
                .allMatch(path -> path.startsWith("00000"));
    }

    @Test
    void findAllInfiniteScrollWhenSecond() {
        List<CommentV2> comments = commentRepository.findAllInfiniteScroll(1L, "0000000009", 10L); // 테스트 편의를 위해 lastPath = 00000 00009 지정

        assertThat(comments).hasSize(10);
        assertThat(comments).extracting(CommentV2::getPath)
                .allMatch(path -> path.startsWith("00001"));
    }
}
