package com.example.boardservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * FAQ 엔티티
 * 자주 묻는 질문과 답변을 관리
 */
@Entity
@Table(name = "faqs")
@Data
public class FAQ {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "faqId")
    private Long faqId;

    @Column(name = "category", length = 50, nullable = false)
    private String category;

    @Column(name = "question", columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(name = "answer", columnDefinition = "TEXT", nullable = false)
    private String answer;

    @Column(name = "viewCount", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.viewCount == null) this.viewCount = 0;
    }
}
