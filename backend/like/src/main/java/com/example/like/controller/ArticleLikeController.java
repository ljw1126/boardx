package com.example.like.controller;

import com.example.like.service.ArticleLikeService;
import com.example.like.service.response.ArticleLikeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ArticleLikeController {
    private final ArticleLikeService articleLikeService;

    @GetMapping("/v1/article-like/article/{articleId}/user/{userId}")
    public ResponseEntity<ArticleLikeResponse> read(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        return ResponseEntity.ok(articleLikeService.read(articleId, userId));
    }

    @PostMapping("/v1/article-like/article/{articleId}/user/{userId}")
    public ResponseEntity<Void> like(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        log.info("ArticleLikeController like articleId = {}, userId = {}", articleId, userId);
        articleLikeService.like(articleId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(null);
    }

    @DeleteMapping("/v1/article-like/article/{articleId}/user/{userId}")
    public ResponseEntity<Void> unlike(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        articleLikeService.unlike(articleId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/v1/article-like/article/{articleId}/count")
    public ResponseEntity<Long> count(@PathVariable("articleId") Long articleId) {
        return ResponseEntity.ok(articleLikeService.count(articleId));
    }
}
