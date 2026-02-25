package com.example.member.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class Member {

    @Id
    @Column(name = "userId", length = 50)
    private String userId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;

    @JsonIgnore
    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "userType", nullable = false)
    private String userType = "member";

    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    // 로그인 실패 관련 필드
    @Column(name = "failedLoginAttempts", columnDefinition = "INT DEFAULT 0")
    private Integer failedLoginAttempts = 0;

    @Column(name = "accountLockedUntil")
    private LocalDateTime accountLockedUntil;

    public enum UserType {
        MEMBER, ADMIN, STORE_OWNER
    }

    // 계정 잠금 확인 메서드
    public boolean isAccountLocked() {
        if (accountLockedUntil == null) {
            return false;
        }
        if (LocalDateTime.now().isAfter(accountLockedUntil)) {
            // 잠금 시간이 지났으면 잠금 해제
            accountLockedUntil = null;
            failedLoginAttempts = 0;
            return false;
        }
        return true;
    }

    // 로그인 실패 처리
    public void recordLoginFailure() {
        if (failedLoginAttempts == null) {
            failedLoginAttempts = 0;
        }
        failedLoginAttempts++;
        if (failedLoginAttempts >= 5) {
            // 5회 실패 시 30분 잠금
            accountLockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }

    // 로그인 성공 시 실패 횟수 초기화
    public void resetLoginFailures() {
        failedLoginAttempts = 0;
        accountLockedUntil = null;
    }

    // Getter에서 null 처리
    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts != null ? failedLoginAttempts : 0;
    }
}