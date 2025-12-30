package com.du.boardservice.repository;

import com.du.boardservice.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    
    // 상단 고정 게시글 조회
    List<Board> findByIsPinnedTrueOrderByCreatedAtDesc();
    
    // 일반 게시글 조회 (최신순)
    List<Board> findByIsPinnedFalseOrderByCreatedAtDesc();
    
    // 특정 작성자의 게시글 조회
    List<Board> findByAuthorOrderByCreatedAtDesc(String author);
    
    // 조회수 증가
    @Modifying
    @Query("UPDATE Board b SET b.viewCount = b.viewCount + 1 WHERE b.id = :id")
    void incrementViewCount(@Param("id") Long id);
    
    // 좋아요 증가
    @Modifying
    @Query("UPDATE Board b SET b.likeCount = b.likeCount + 1 WHERE b.id = :id")
    void incrementLikeCount(@Param("id") Long id);
}
