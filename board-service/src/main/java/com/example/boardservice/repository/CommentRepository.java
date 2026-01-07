package com.example.boardservice.repository;

import com.example.boardservice.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // 특정 게시글의 댓글 조회
    List<Comment> findByBoardIdOrderByCreatedAtAsc(Long boardId);
    
    // 특정 작성자의 댓글 조회
    List<Comment> findByAuthorOrderByCreatedAtDesc(String author);
}
