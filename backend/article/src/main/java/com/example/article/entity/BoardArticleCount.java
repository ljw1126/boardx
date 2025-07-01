package com.example.article.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "board_article_count")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BoardArticleCount {

    @Id
    private Long boardId;
    private Long articleCount;

    public static BoardArticleCount of(Long boardId, Long articleCount) {
        return new BoardArticleCount(boardId, articleCount);
    }
}
