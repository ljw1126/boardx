package com.example.article.service.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArticleUpdateRequest {
    private String title;
    private String content;
}
