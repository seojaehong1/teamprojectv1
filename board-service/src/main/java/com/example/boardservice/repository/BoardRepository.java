package com.example.boardservice.repository;

import com.example.boardservice.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // 전체 게시글 조회 (최신순)
    List<Board> findAllByOrderByCreatedAtDesc();

    // 특정 사용자의 게시글 조회
    List<Board> findByUserIdOrderByCreatedAtDesc(String userId);
}
