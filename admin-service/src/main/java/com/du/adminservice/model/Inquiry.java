package com.du.adminservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry")
@Data
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    @JsonProperty("id")
    private Long inquiryId;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "PENDING"; // PENDING, ANSWERED

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Column(name = "answered_by", length = 50)
    private String answeredBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;
}
