package com.example.member.repository;

import com.example.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {

    Optional<Member> findByName(String name);

    Optional<Member> findByUserId(String userId);

    Optional<Member> findByEmail(String email);

    // 아이디 찾기용
    Optional<Member> findByNameAndEmail(String name, String email);

    // 비밀번호 찾기용 - 본인 확인 (userId + email)
    Optional<Member> findByUserIdAndEmail(String userId, String email);

    // 비밀번호 찾기용 - 전체 확인 (name + userId + email)
    Optional<Member> findByNameAndUserIdAndEmail(String name, String userId, String email);

    boolean existsByName(String name);

    boolean existsByUserId(String userId);

    boolean existsByEmail(String email);

    void deleteByUserId(String userId);

    // 검색 기능 (이름, 이메일로 검색) - admin 최상단
    // keyword는 Service에서 '%keyword%' 형태로 전달해야 함
    @Query("SELECT m FROM Member m WHERE " +
           "(LOWER(m.name) LIKE LOWER(:keyword) OR " +
           "LOWER(m.email) LIKE LOWER(:keyword)) " +
           "ORDER BY CASE WHEN m.userId = 'admin' THEN 0 ELSE 1 END, m.createdAt DESC")
    List<Member> searchByKeyword(@Param("keyword") String keyword);

    // 전체 조회 (admin 최상단, 나머지는 최신순)
    @Query("SELECT m FROM Member m ORDER BY CASE WHEN m.userId = 'admin' THEN 0 ELSE 1 END, m.createdAt DESC")
    List<Member> findAllOrderByAdminFirst();

    // 회원 유형별 조회 - admin 최상단
    @Query("SELECT m FROM Member m WHERE LOWER(m.userType) = LOWER(:userType) " +
           "ORDER BY CASE WHEN m.userId = 'admin' THEN 0 ELSE 1 END, m.createdAt DESC")
    List<Member> findByUserTypeOrderByAdminFirst(@Param("userType") String userType);

    // 회원 유형별 검색 (이름, 이메일로 검색) - admin 최상단
    // keyword는 Service에서 '%keyword%' 형태로 전달해야 함
    @Query("SELECT m FROM Member m WHERE LOWER(m.userType) = LOWER(:userType) AND " +
           "(LOWER(m.name) LIKE LOWER(:keyword) OR " +
           "LOWER(m.email) LIKE LOWER(:keyword)) " +
           "ORDER BY CASE WHEN m.userId = 'admin' THEN 0 ELSE 1 END, m.createdAt DESC")
    List<Member> searchByUserTypeAndKeyword(@Param("userType") String userType, @Param("keyword") String keyword);
}
