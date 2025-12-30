package com.example.member.repository;

import com.example.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 회원 데이터 접근 Repository (JPA)
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    // username으로 회원 조회
    Optional<Member> findByUsername(String username);
    
    // userId로 회원 조회 (로그인 시 사용)
    Optional<Member> findByUserId(String userId);
    
    // email로 회원 조회 (소셜 로그인 시 사용)
    Optional<Member> findByEmail(String email);
    
    // username 존재 여부 확인
    boolean existsByUsername(String username);
    
    // userId 중복 체크
    boolean existsByUserId(String userId);
    
    // email 중복 체크
    boolean existsByEmail(String email);
}
