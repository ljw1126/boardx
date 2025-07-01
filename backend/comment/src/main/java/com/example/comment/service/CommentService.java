package com.example.comment.service;

import com.example.comment.entity.ArticleCommentCount;
import com.example.comment.entity.Comment;
import com.example.comment.repository.ArticleCommentCountRepository;
import com.example.comment.repository.CommentRepository;
import com.example.comment.service.request.CommentCreateRequest;
import com.example.comment.service.response.CommentPageResponse;
import com.example.comment.service.response.CommentResponse;
import com.example.snowflake.Snowflake;
import com.example.support.PageLimitCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    private final Snowflake snowflake = new Snowflake();
    private final CommentRepository commentRepository;
    private final ArticleCommentCountRepository articleCommentCountRepository;

    public CommentResponse create(CommentCreateRequest request) {
        Comment parent = findParent(request);
        Comment saved = commentRepository.save(
                Comment.of(
                        snowflake.nextId(),
                        request.getContent(),
                        parent == null ? null : parent.getParentCommentId(),
                        request.getArticleId(),
                        request.getWriterId()
                )
        );

        int result = articleCommentCountRepository.increase(saved.getArticleId());
        if(result == 0) {
            articleCommentCountRepository.save(ArticleCommentCount.of(saved.getArticleId(), 1L));
        }

        return CommentResponse.from(saved);
    }

    private Comment findParent(CommentCreateRequest request) {
        Long parentCommentId = request.getParentCommentId();
        if (parentCommentId == null) {
            return null;
        }

        return commentRepository.findById(parentCommentId)
                .filter(not(Comment::getDeleted))
                .filter(Comment::isRoot)
                .orElseThrow();
    }

    @Transactional(readOnly = true)
    public CommentResponse read(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        return CommentResponse.from(comment);
    }

    @Transactional(readOnly = true)
    public CommentPageResponse readAll(Long articleId, Long page, Long pageSize) {
        List<Comment> comments = commentRepository.findAll(articleId, (page - 1) * pageSize, pageSize);
        Long count = commentRepository.count(articleId, PageLimitCalculator.pageLimit(page, pageSize, 10L));

        return CommentPageResponse.of(
                comments.stream().map(CommentResponse::from).toList(),
                count
        );
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> readAllInfiniteScroll(Long articleId, Long lastParentCommentId, Long lastCommentId, Long limit) {
        List<Comment> comments = (lastParentCommentId == null || lastCommentId == null)
                ? commentRepository.findAllInfiniteScroll(articleId, limit)
                : commentRepository.findAllInfiniteScroll(articleId, lastParentCommentId, lastCommentId, limit);

        return comments.stream().map(CommentResponse::from).toList();
    }

    public void delete(Long commentId) {
        commentRepository.findById(commentId)
                .filter(not(Comment::getDeleted))
                .ifPresent(comment -> {
                    if (hasChildren(comment)) {
                        comment.delete();
                    } else {
                        delete(comment);
                    }
                });
    }

    private boolean hasChildren(Comment comment) {
        return commentRepository.countBy(comment.getArticleId(), comment.getCommentId(), 2L) == 2L;
    }

    private void delete(Comment comment) {
        commentRepository.delete(comment);
        articleCommentCountRepository.decrease(comment.getArticleId());
        
        if (!comment.isRoot()) {
            commentRepository.findById(comment.getParentCommentId())
                    .filter(Comment::getDeleted)
                    .filter(not(this::hasChildren))
                    .ifPresent(this::delete);
        }
    }
}
