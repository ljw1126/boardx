package com.example.comment.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommentPathTest {

    @Test
    void create() {
        // 00000 생성
        assertCommentPath(CommentPath.create(""), null, "00000");

        // 00000
        // 00001 생성
        assertCommentPath(CommentPath.create(""), "00000", "00001");

        // 00000
        //       00000 생성
        assertCommentPath(CommentPath.create("00000"), null, "0000000000");

        // 0000z
        //       abcdz
        //            zzzzz
        //                 zzzzz
        //       abce0 생성
        assertCommentPath(CommentPath.create("0000z"), "0000zabcdzzzzzzzzzzz", "0000zabce0");
    }

    void assertCommentPath(CommentPath parentPath, String descendantsTopPath, String expected) {
        CommentPath childCommentPath = parentPath.createChildCommentPath(descendantsTopPath);

        assertThat(childCommentPath).isEqualTo(CommentPath.create(expected));
    }

    @Test
    void createChildCommentPathIfMaxDepth() {
        CommentPath commentPath = CommentPath.create("zzzzz".repeat(5));

        assertThatThrownBy(() -> commentPath.createChildCommentPath(null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createChildCommentPathIfChunkOverflow() {
        CommentPath commentPath = CommentPath.create("");

        assertThatThrownBy(() -> commentPath.createChildCommentPath("zzzzz"))
                .isInstanceOf(IllegalStateException.class);
    }
}
