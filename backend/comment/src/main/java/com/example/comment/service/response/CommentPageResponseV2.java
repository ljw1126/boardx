package com.example.comment.service.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentPageResponseV2 {
    private List<CommentResponseV2> comments;
    private Long commentCount;

    public static CommentPageResponseV2 of(List<CommentResponseV2> comments, Long commentCount) {
        return new CommentPageResponseV2(comments, commentCount);
    }
}
