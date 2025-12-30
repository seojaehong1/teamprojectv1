package com.du.boardservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "board")
@Data
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;  // 제목
    
    @Column(nullable = false, length = 5000)
    private String content;  // 내용
    
    @Column(nullable = false)
    private String author;  // 작성자 (userId)
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;  // 작성일
    
    @Column(name = "view_count")
    private Integer viewCount = 0;  // 조회수
    
    @Column(name = "like_count")
    private Integer likeCount = 0;  // 좋아요
    
    @Column(name = "is_pinned")
    private Boolean isPinned = false;  // 상단 고정 여부
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.viewCount == null) this.viewCount = 0;
        if (this.likeCount == null) this.likeCount = 0;
        if (this.isPinned == null) this.isPinned = false;
    }
}
