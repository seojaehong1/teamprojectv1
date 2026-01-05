package com.example.boardservice.repository;

import com.example.boardservice.model.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeRepository extends JpaRepository<com.example.boardservice.model.Notice, Long> {

    // 고정 공지 조회 (상단 고정)
    List<com.example.boardservice.model.Notice> findByIsPinnedTrueOrderByCreatedAtDesc();

    // 일반 공지 조회 (최신순)
    List<com.example.boardservice.model.Notice> findByIsPinnedFalseOrderByCreatedAtDesc();

    // 전체 공지 조회 (고정 공지 먼저, 그 다음 최신순)
    @Query("SELECT n FROM Notice n ORDER BY n.isPinned DESC, n.createdAt DESC")
    List<com.example.boardservice.model.Notice> findAllOrderByPinnedAndCreatedAt();

    // 페이징 처리된 전체 공지 조회 (고정 공지 먼저, 그 다음 최신순)
    @Query("SELECT n FROM Notice n ORDER BY n.isPinned DESC, n.createdAt DESC")
    Page<com.example.boardservice.model.Notice> findAllOrderByPinnedAndCreatedAtPaged(Pageable pageable);

    // 조회수 증가
    @Modifying
    @Query("UPDATE Notice n SET n.viewCount = n.viewCount + 1 WHERE n.noticeId = :id")
    void incrementViewCount(@Param("id") Long id);

    // 제목으로 검색 (고정 공지 먼저, 최신순)
    @Query("SELECT n FROM Notice n WHERE n.title LIKE %:keyword% ORDER BY n.isPinned DESC, n.createdAt DESC")
    Page<com.example.boardservice.model.Notice> searchByTitleContaining(@Param("keyword") String keyword, Pageable pageable);
}
