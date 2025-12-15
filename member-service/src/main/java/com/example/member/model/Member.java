package com.example.member.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

// 회원 엔티티
@Entity
@Table(name = "users")
@Data
public class Member {
    
    // 회원 고유 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 회원 이름
    @Column(name = "user_name", nullable = false)
    private String username;
    
    // 회원 아이디 (로그인용)
    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;
    
    // 비밀번호 (소셜 로그인은 NULL)
    @Column(nullable = true)
    private String password;
    
    // 이메일
    @Column(unique = true, nullable = false)
    private String email;
    
    // 생년월일 (소셜 로그인은 NULL)
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    // 전화번호 (소셜 로그인은 NULL)
    @Column(name = "phone_num")
    private String phoneNum;
    
    // 로그인 제공자 (DEFAULT, NAVER, GOOGLE)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider = Provider.DEFAULT;
    
    // 권한 (USER, ADMIN)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;
    
    // 권한 enum
    public enum Role {
        USER, ADMIN
    }
    
    // 로그인 제공자 enum
    public enum Provider {
        DEFAULT, NAVER, GOOGLE
    }
}
