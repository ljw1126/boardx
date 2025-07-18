package com.example.view.controller;

import com.example.view.service.ArticleViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ArticleViewController {
    private final ArticleViewService articleViewService;

    @PostMapping("/v1/article-view/article/{articleId}/user/{userId}")
    public ResponseEntity<Long> increase(@PathVariable("articleId") Long articleId, @PathVariable("userId") Long userId) {
        log.info("ArticleViewController articleId  = {}, userId = {}", articleId, userId);
        return ResponseEntity.ok(articleViewService.increase(articleId, userId));
    }

    @GetMapping("/v1/article_view/article/{articleId}/count")
    public ResponseEntity<Long> count(@PathVariable("articleId") Long articleId) {
        return ResponseEntity.ok(articleViewService.count(articleId));
    }
}
