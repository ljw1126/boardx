package com.example.comment.controller;

import com.example.comment.service.CommentService;
import com.example.comment.service.request.CommentCreateRequest;
import com.example.comment.service.response.CommentPageResponse;
import com.example.comment.service.response.CommentResponse;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/v1/comment")
    public ResponseEntity<CommentResponse> create(@RequestBody CommentCreateRequest request) {
        CommentResponse response = commentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/v1/comment/{commentId}")
    public ResponseEntity<CommentResponse> read(@PathVariable("commentId") Long commentId) {
        return ResponseEntity.ok(commentService.read(commentId));
    }

    @GetMapping("/v1/comment")
    public ResponseEntity<CommentPageResponse> readAll(
            @RequestParam("articleId") Long articleId,
            @RequestParam("page") Long page,
            @RequestParam("pageSize") Long pageSize
    ) {
        return ResponseEntity.ok(commentService.readAll(articleId, page, pageSize));
    }

    @GetMapping("/v1/comment/infinite-scroll")
    public ResponseEntity<List<CommentResponse>> readAllInfiniteScroll(
            @RequestParam("articleId") Long articleId,
            @RequestParam(value = "lastParentCommentId", required = false) Long lastParentCommentId,
            @RequestParam(value = "lastCommentId", required = false) Long lastCommentId,
            @RequestParam("pageSize") Long pageSize
    ) {
        List<CommentResponse> commentResponses = commentService.readAllInfiniteScroll(articleId, lastParentCommentId, lastCommentId, pageSize);
        return ResponseEntity.ok(commentResponses);
    }

    @DeleteMapping("/v1/comment/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable("commentId") Long commentId) {
        commentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }

}
