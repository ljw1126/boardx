package com.example.article.service.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArticleCreateRequest {
    private String title;
    private String content;
    private Long writerId;
    private Long boardId;
}
