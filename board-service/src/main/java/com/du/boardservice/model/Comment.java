package com.du.boardservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

// 댓글 엔티티
@Entity
@Table(name = "comment")
@Data
public class Comment {
    
    // 댓글 고유 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 게시글 ID (FK)
    @Column(name = "board_id", nullable = false)
    private Long boardId;
    
    // 댓글 내용
    @Column(nullable = false, length = 1000)
    private String content;
    
    // 작성자 (userId)
    @Column(nullable = false)
    private String author;
    
    // 작성일시
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // 저장 전 작성일시 자동 설정
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
