package com.example.article.service.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ArticlePageResponse {
    private List<ArticleResponse> articles;
    private Long count;
}
