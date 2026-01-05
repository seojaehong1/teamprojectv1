package com.example.boardservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 문의 게시판 엔티티
 * 사용자가 문의를 작성하고 관리자가 답변
 */
@Entity
@Table(name = "inquiries")
@Data
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiryId")
    private Long inquiryId;

    @Column(name = "userId", length = 50, nullable = false)
    private String userId;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "pending";

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Column(name = "answeredBy", length = 50)
    private String answeredBy;

    @Column(name = "answeredAt")
    private LocalDateTime answeredAt;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "pending";
    }

    public enum Status {
        PENDING, ANSWERED
    }
}
