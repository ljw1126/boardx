package com.example.article.repository;

import com.example.article.entity.BoardArticleCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardArticleCountRepository extends JpaRepository<BoardArticleCount, Long> {
    @Query(value = "update board_article_count set article_count = article_count + 1 where board_id = :boardId",
            nativeQuery = true)
    @Modifying(clearAutomatically = true)
    int increase(@Param("boardId") Long boardId);

    @Query(value = "update board_article_count set article_count = article_count - 1 where board_id = :boardId",
            nativeQuery = true)
    @Modifying(clearAutomatically = true)
    int decrease(@Param("boardId") Long boardId);
}
