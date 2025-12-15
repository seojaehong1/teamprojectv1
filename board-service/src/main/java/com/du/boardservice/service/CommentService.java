package com.du.boardservice.service;

import com.du.boardservice.model.Comment;
import com.du.boardservice.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    
    private final CommentRepository commentRepository;
    
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }
    
    // 특정 게시글의 댓글 목록 조회
    public List<Comment> getCommentsByBoardId(Long boardId) {
        return commentRepository.findByBoardIdOrderByCreatedAtAsc(boardId);
    }
    
    // 댓글 작성
    public Comment createComment(Comment comment) {
        return commentRepository.save(comment);
    }
    
    // 댓글 삭제 (본인 또는 ADMIN만 가능)
    public void deleteComment(Long id, String author, String role) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));
        
        // ADMIN은 모든 댓글 삭제 가능, 일반 사용자는 본인 댓글만 삭제 가능
        if (!"ADMIN".equals(role) && !comment.getAuthor().equals(author)) {
            throw new RuntimeException("본인의 댓글만 삭제할 수 있습니다.");
        }
        
        commentRepository.deleteById(id);
    }
    
    // 특정 작성자의 댓글 목록 조회
    public List<Comment> getCommentsByAuthor(String author) {
        return commentRepository.findByAuthorOrderByCreatedAtDesc(author);
    }
}
