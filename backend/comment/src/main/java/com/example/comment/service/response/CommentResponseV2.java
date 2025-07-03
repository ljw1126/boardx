package com.example.comment.service.response;

import com.example.comment.entity.CommentV2;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseV2 {
    private Long commentId;
    private String content;
    private Long articleId;
    private Long writerId;
    private Boolean deleted;
    private String path;
    private LocalDateTime createdAt;

    public static CommentResponseV2 from(CommentV2 comment) {
        CommentResponseV2 response = new CommentResponseV2();
        response.commentId = comment.getCommentId();
        response.content = comment.getContent();
        response.articleId = comment.getArticleId();
        response.writerId = comment.getWriterId();
        response.deleted = comment.getDeleted();
        response.path = comment.getCommentPath().getPath();
        response.createdAt = comment.getCreatedAt();

        return response;
    }
}
