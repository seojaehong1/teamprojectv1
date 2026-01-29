package com.example.boardservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 공지사항 엔티티
 * 관리자만 생성/수정/삭제 가능, 일반 사용자는 읽기만 가능
 */
@Entity
@Table(name = "notices")
@Data
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "noticeId")
    @JsonProperty("id")
    private Long noticeId;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "author", length = 50, nullable = false)
    private String author;

    @Column(name = "isPinned", nullable = false)
    private Boolean isPinned = false;

    @Column(name = "viewCount", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.viewCount == null) this.viewCount = 0;
        if (this.isPinned == null) this.isPinned = false;
    }
}
