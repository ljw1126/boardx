package com.example.articleread.service.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ArticleReadPageResponse {
    private List<ArticleReadResponse> articles;
    private Long articleCount;

    public static ArticleReadPageResponse of(List<ArticleReadResponse> articles, Long articleCount) {
        ArticleReadPageResponse response = new ArticleReadPageResponse();
        response.articles = articles;
        response.articleCount = articleCount;
        return response;
    }
}
