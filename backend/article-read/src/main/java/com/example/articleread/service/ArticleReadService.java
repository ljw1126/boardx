package com.example.articleread.service;

import com.example.articleread.client.ArticleClient;
import com.example.articleread.client.CommentClient;
import com.example.articleread.client.LikeClient;
import com.example.articleread.client.ViewClient;
import com.example.articleread.repository.ArticleIdListRepository;
import com.example.articleread.repository.ArticleQueryModel;
import com.example.articleread.repository.ArticleQueryModelRepository;
import com.example.articleread.repository.BoardArticleCountRepository;
import com.example.articleread.service.eventhandler.EventHandler;
import com.example.articleread.service.response.ArticleReadPageResponse;
import com.example.articleread.service.response.ArticleReadResponse;
import com.example.event.Event;
import com.example.event.EventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleReadService {
    private final ArticleClient articleClient;
    private final CommentClient commentClient;
    private final LikeClient likeClient;
    private final ViewClient viewClient;
    private final ArticleIdListRepository articleIdListRepository;
    private final ArticleQueryModelRepository articleQueryModelRepository;
    private final BoardArticleCountRepository boardArticleCountRepository;
    private final List<EventHandler> eventHandlers;

    public void handleEvent(Event<EventPayload> event) {
        for(EventHandler eventHandler : eventHandlers) {
            if(eventHandler.supports(event)) {
                eventHandler.handle(event);
            }
        }
    }

    public ArticleReadResponse read(Long articleId) {
        ArticleQueryModel articleQueryModel = articleQueryModelRepository.read(articleId)
                .or(() -> fetch(articleId))
                .orElseThrow();

        return ArticleReadResponse.of(
                articleQueryModel,
                viewClient.count(articleId)
        );
    }

    private Optional<ArticleQueryModel> fetch(Long articleId) {
        Optional<ArticleQueryModel> articleQueryModelOptional = articleClient.read(articleId)
                .map(article -> ArticleQueryModel.of(
                        article,
                        commentClient.count(articleId),
                        likeClient.count(articleId)
                ));

        articleQueryModelOptional
                .ifPresent(data -> articleQueryModelRepository.create(data, Duration.ofDays(1L)));
        log.info("[ArticleReadService.fetch] fetch data. articleId={}, isPresent={}", articleId, articleQueryModelOptional.isPresent());

        return articleQueryModelOptional;
    }

    public ArticleReadPageResponse readAll(Long boardId, Long page, Long pageSize) {
        List<Long> articleIds = readAllArticleIds(boardId, page, pageSize);
        List<ArticleReadResponse> articles = readAll(articleIds);
        Long articleCount = articleCount(boardId);

        return ArticleReadPageResponse.of(articles, articleCount);
    }

    private List<ArticleReadResponse> readAll(List<Long> articleIds) {
        Map<Long, ArticleQueryModel> articleQueryModelMap = articleQueryModelRepository.readAll(articleIds);
        return articleIds.stream()
                .map(articleId -> articleQueryModelMap.containsKey(articleId) ? articleQueryModelMap.get(articleId) : fetch(articleId).orElse(null))
                .filter(Objects::nonNull)
                .map(articleQueryModel -> ArticleReadResponse.of(articleQueryModel, viewClient.count(articleQueryModel.getArticleId())))
                .toList();
    }

    private List<Long> readAllArticleIds(Long boardId, Long page, Long pageSize) {
        List<Long> articleIds = articleIdListRepository.readAll(boardId, (page - 1) * pageSize, pageSize);
        if(pageSize == articleIds.size()) {
            log.info("[ArticleReadService.readAllArticleIds] return redis data");
            return articleIds;
        }

        log.info("[ArticleReadService.readAllArticleIds] return origin data");
        return articleClient.readAll(boardId, page, pageSize)
                .getArticles()
                .stream()
                .map(ArticleClient.ArticleResponse::getArticleId)
                .toList();
    }

    private Long articleCount(Long boardId) {
        Long result = boardArticleCountRepository.read(boardId);
        if(result != null) {
            log.info("[ArticleReadService.articleCount] return redis data");
            return result;
        }

        log.info("[ArticleReadService.articleCount] return origin data");
        Long articleCount = articleClient.count(boardId);
        boardArticleCountRepository.save(boardId, articleCount);
        return articleCount;
    }

    public List<ArticleReadResponse> readAllInfiniteScroll(Long boardId, Long lastArticleId, Long pageSize) {
        List<Long> articleIds = readAllInfiniteScrollArticleIds(boardId, lastArticleId, pageSize);
        return readAll(articleIds);
    }

    private List<Long> readAllInfiniteScrollArticleIds(Long boardId, Long lastArticleId, Long pageSize) {
        List<Long> articleIds = articleIdListRepository.readAllInfiniteScroll(boardId, lastArticleId, pageSize);
        if(pageSize == articleIds.size()) {
            log.info("[ArticleReadService.readAllInfiniteScrollArticleIds] return redis data.");
            return articleIds;
        }

        log.info("[ArticleReadService.readAllInfiniteScrollArticleIds] return origin data.");
        return articleClient.readAllInfiniteScroll(boardId, lastArticleId, pageSize)
                .stream()
                .map(ArticleClient.ArticleResponse::getArticleId)
                .toList();
    }

}
