package com.example.member.repository;

import com.example.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
