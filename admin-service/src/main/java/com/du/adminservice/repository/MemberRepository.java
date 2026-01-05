package com.du.adminservice.repository;

import com.du.adminservice.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByUserId(String userId);

    // 전체 조회 (admin 최상단, 나머지는 최신순)
    @Query("SELECT m FROM Member m ORDER BY CASE WHEN m.userId = 'admin' THEN 0 ELSE 1 END, m.createdAt DESC")
    Page<Member> findAllOrderByAdminFirst(Pageable pageable);

    // 검색 기능 (이름, 이메일로 검색) - admin 최상단
    @Query("SELECT m FROM Member m WHERE " +
           "(:keyword IS NULL OR " +
           "m.name LIKE %:keyword% OR " +
           "m.email LIKE %:keyword%) " +
           "ORDER BY CASE WHEN m.userId = 'admin' THEN 0 ELSE 1 END, m.createdAt DESC")
    Page<Member> searchMembers(@Param("keyword") String keyword, Pageable pageable);

    // 회원 유형별 조회 - admin 최상단
    @Query("SELECT m FROM Member m WHERE m.userType = :userType " +
           "ORDER BY CASE WHEN m.userId = 'admin' THEN 0 ELSE 1 END, m.createdAt DESC")
    Page<Member> findByUserTypeOrderByAdminFirst(@Param("userType") String userType, Pageable pageable);

    // 회원 유형별 검색 (이름, 이메일로 검색) - admin 최상단
    @Query("SELECT m FROM Member m WHERE m.userType = :userType AND " +
           "(:keyword IS NULL OR " +
           "m.name LIKE %:keyword% OR " +
           "m.email LIKE %:keyword%) " +
           "ORDER BY CASE WHEN m.userId = 'admin' THEN 0 ELSE 1 END, m.createdAt DESC")
    Page<Member> searchMembersByType(@Param("userType") String userType,
                                      @Param("keyword") String keyword,
                                      Pageable pageable);
}
