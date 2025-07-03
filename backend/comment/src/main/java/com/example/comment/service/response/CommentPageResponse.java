package com.example.comment.service.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentPageResponse {
    private List<CommentResponse> comments;
    private Long commentCount;

    public static CommentPageResponse of(List<CommentResponse> comments, Long commentCount) {
        return new CommentPageResponse(comments, commentCount);
    }
}
