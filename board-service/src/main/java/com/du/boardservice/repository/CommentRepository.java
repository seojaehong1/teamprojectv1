package com.du.boardservice.repository;

import com.du.boardservice.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // 특정 게시글의 댓글 조회
    List<Comment> findByBoardIdOrderByCreatedAtAsc(Long boardId);
    
    // 특정 작성자의 댓글 조회
    List<Comment> findByAuthorOrderByCreatedAtDesc(String author);
}
