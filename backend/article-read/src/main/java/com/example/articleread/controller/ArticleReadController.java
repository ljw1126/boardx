package com.example.articleread.controller;

import com.example.articleread.service.ArticleReadService;
import com.example.articleread.service.response.ArticleReadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ArticleReadController {
    private final ArticleReadService articleReadService;

    @GetMapping("/v1/article/{articleId}")
    public ResponseEntity<ArticleReadResponse> read(@PathVariable Long articleId) {
        return ResponseEntity.ok(articleReadService.read(articleId));
    }
}
