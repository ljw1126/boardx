package com.example.article.service;

import com.example.article.entity.Article;
import com.example.article.entity.BoardArticleCount;
import com.example.article.repository.ArticleRepository;
import com.example.article.repository.BoardArticleCountRepository;
import com.example.article.service.request.ArticleCreateRequest;
import com.example.article.service.request.ArticleUpdateRequest;
import com.example.article.service.response.ArticlePageResponse;
import com.example.article.service.response.ArticleResponse;
import com.example.snowflake.Snowflake;
import com.example.support.PageLimitCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleService {
    private final Snowflake snowflake = new Snowflake();

    private final ArticleRepository articleRepository;
    private final BoardArticleCountRepository boardArticleCountRepository;

    public ArticleResponse create(ArticleCreateRequest request) {
        Article saved = articleRepository.save(Article.create(snowflake.nextId(), request.getTitle(), request.getContent(), request.getBoardId(), request.getWriterId()));
        int result = boardArticleCountRepository.increase(request.getBoardId());
        if(result == 0) {
            boardArticleCountRepository.save(BoardArticleCount.of(request.getBoardId(), 1L));
        }

        return ArticleResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ArticleResponse read(Long articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow();
        return ArticleResponse.from(article);
    }

    @Transactional(readOnly = true)
    public ArticlePageResponse readAll(Long boardId, Long page, Long pageSize) {
        List<Article> articles = articleRepository.findAll(boardId, (page - 1) * pageSize, pageSize);
        Long count = articleRepository.count(boardId, PageLimitCalculator.pageLimit(page, pageSize, 10L));
        return new ArticlePageResponse(
                articles.stream().map(ArticleResponse::from).toList(),
                count
        );
    }

    public ArticleResponse update(Long articleId, ArticleUpdateRequest request) {
        Article article = articleRepository.findById(articleId).orElseThrow();
        article.update(request.getTitle(), request.getContent());
        return ArticleResponse.from(article);
    }

    public void delete(Long articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow();
        articleRepository.delete(article);
        boardArticleCountRepository.decrease(article.getBoardId());
    }

}
