package com.example.article.controller;

import com.example.article.service.ArticleService;
import com.example.article.service.request.ArticleCreateRequest;
import com.example.article.service.request.ArticleUpdateRequest;
import com.example.article.service.response.ArticlePageResponse;
import com.example.article.service.response.ArticleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;

    @PostMapping("/v1/article")
    public ResponseEntity<ArticleResponse> create(@RequestBody ArticleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(articleService.create(request));
    }

    @GetMapping("/v1/article/{articleId}")
    public ResponseEntity<ArticleResponse> read(@PathVariable Long articleId) {
        return ResponseEntity.ok(articleService.read(articleId));
    }

    @GetMapping("/v1/article")
    public ResponseEntity<ArticlePageResponse> readAll(@RequestParam("boardId") Long boardId,
                                       @RequestParam("page") Long page,
                                       @RequestParam("pageSize") Long pageSize
    ) {
        return ResponseEntity.ok(articleService.readAll(boardId, page, pageSize));
    }

    @PutMapping("/v1/article/{articleId}")
    public ResponseEntity<ArticleResponse> update(@PathVariable Long articleId, @RequestBody ArticleUpdateRequest request) {
        return ResponseEntity.ok(articleService.update(articleId, request));
    }

    @DeleteMapping("/v1/article/{articleId}")
    public ResponseEntity<Void> delete(@PathVariable Long articleId) {
        articleService.delete(articleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/v1/article/infinite-scroll")
    public ResponseEntity<List<ArticleResponse>> readAllInfiniteScroll(@RequestParam("boardId") Long boardId,
                                                                       @RequestParam("pageSize") Long pageSize,
                                                                       @RequestParam(value = "lastArticleId", required = false) Long lastArticleId
    ) {
        return ResponseEntity.ok(articleService.readAllInfiniteScroll(boardId, pageSize, lastArticleId));
    }

    @GetMapping("/v1/article/board/{boardId}/count")
    public ResponseEntity<Long> count(@PathVariable Long boardId) {
        return ResponseEntity.ok(articleService.count(boardId));
    }
}
