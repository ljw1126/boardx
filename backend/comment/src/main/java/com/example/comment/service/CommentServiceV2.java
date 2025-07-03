package com.example.comment.service;

import com.example.comment.entity.ArticleCommentCount;
import com.example.comment.entity.CommentPath;
import com.example.comment.entity.CommentV2;
import com.example.comment.repository.ArticleCommentCountRepository;
import com.example.comment.repository.CommentRepositoryV2;
import com.example.comment.service.request.CommentCreateRequestV2;
import com.example.comment.service.response.CommentPageResponseV2;
import com.example.comment.service.response.CommentResponseV2;
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
public class CommentServiceV2 {
    private final Snowflake snowflake = new Snowflake();
    private final CommentRepositoryV2 commentRepository;
    private final ArticleCommentCountRepository articleCommentCountRepository;

    public CommentResponseV2 create(CommentCreateRequestV2 request) {
        CommentV2 parent = findParent(request);
        CommentPath parentCommentPath = parent == null ? CommentPath.create("") : parent.getCommentPath();
        String descendantsTopPath = commentRepository.findDescendantsTopPath(request.getArticleId(), parentCommentPath.getPath())
                .orElse(null);

        CommentV2 saved = commentRepository.save(
                CommentV2.of(
                        snowflake.nextId(),
                        request.getContent(),
                        request.getArticleId(),
                        request.getWriterId(),
                        parentCommentPath.createChildCommentPath(descendantsTopPath)
                )
       );

        int result = articleCommentCountRepository.increase(request.getArticleId());
        if(result == 0) {
            articleCommentCountRepository.save(ArticleCommentCount.of(request.getArticleId(), 1L));
        }

        return CommentResponseV2.from(saved);
    }

    private CommentV2 findParent(CommentCreateRequestV2 request) {
        return commentRepository.findByPath(request.getParentPath())
                .filter(not(CommentV2::getDeleted))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public CommentResponseV2 read(Long commentId) {
        return CommentResponseV2.from(commentRepository.findById(commentId).orElseThrow());
    }

    @Transactional(readOnly = true)
    public CommentPageResponseV2 readAll(Long articleId, Long page, Long pageSize) {
        List<CommentV2> comments = commentRepository.findAll(articleId, (page - 1) * pageSize, pageSize);
        Long commentCount = commentRepository.count(articleId, PageLimitCalculator.pageLimit(page, pageSize, 10L));

        return CommentPageResponseV2.of(
                comments.stream().map(CommentResponseV2::from).toList(),
                commentCount
        );
    }

    @Transactional(readOnly = true)
    public List<CommentResponseV2> readAllInfiniteScroll(Long articleId, String lastPath, Long pageSize) {
       List<CommentV2> comments = (lastPath == null)
               ? commentRepository.findAllInfiniteScroll(articleId, pageSize)
               : commentRepository.findAllInfiniteScroll(articleId, lastPath, pageSize);

       return comments.stream()
               .map(CommentResponseV2::from)
               .toList();
    }

    public void delete(Long commentId) {
        commentRepository.findById(commentId)
                .filter(not(CommentV2::getDeleted))
                .ifPresent(comment -> {
                    if(hasChildren(comment)) {
                        comment.delete();
                    } else {
                        delete(comment);
                    }
                });
    }

    private boolean hasChildren(CommentV2 comment) {
        return commentRepository.findDescendantsTopPath(comment.getArticleId(), comment.getPath())
                .isPresent();
    }

    private void delete(CommentV2 comment) {
        commentRepository.delete(comment);
        articleCommentCountRepository.decrease(comment.getArticleId());

        if(!comment.isRoot()) {
            commentRepository.findByPath(comment.getCommentPath().getParentPath())
                    .filter(CommentV2::getDeleted)
                    .filter(not(this::hasChildren))
                    .ifPresent(this::delete);
        }
    }
}
