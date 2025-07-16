package com.example.comment.controller;

import com.example.comment.service.CommentServiceV2;
import com.example.comment.service.request.CommentCreateRequestV2;
import com.example.comment.service.response.CommentPageResponseV2;
import com.example.comment.service.response.CommentResponseV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 열겨형 path 활용한 무한 depth 방식
@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentControllerV2 {
    private final CommentServiceV2 commentService;

    @PostMapping("/v2/comment")
    public ResponseEntity<CommentResponseV2> create(@RequestBody CommentCreateRequestV2 request) {
        log.info("CommentControllerV2 request = {}", request);
        CommentResponseV2 response = commentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/v2/comment/{commentId}")
    public ResponseEntity<CommentResponseV2> read(@PathVariable("commentId") Long commentId) {
        return ResponseEntity.ok(commentService.read(commentId));
    }

    @GetMapping("/v2/comment")
    public ResponseEntity<CommentPageResponseV2> readAll(
            @RequestParam("articleId") Long articleId,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize
    ) {
        return ResponseEntity.ok(commentService.readAll(articleId, page, pageSize));
    }

    @GetMapping("/v2/comment/infinite-scroll")
    public ResponseEntity<List<CommentResponseV2>> readAllInfiniteScroll(
            @RequestParam("articleId") Long articleId,
            @RequestParam(value = "lastPath", required = false) String lastPath,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize
    ) {
        List<CommentResponseV2> commentResponses = commentService.readAllInfiniteScroll(articleId, lastPath, pageSize);
        return ResponseEntity.ok(commentResponses);
    }

    @DeleteMapping("/v2/comment/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable("commentId") Long commentId) {
        commentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }
}
