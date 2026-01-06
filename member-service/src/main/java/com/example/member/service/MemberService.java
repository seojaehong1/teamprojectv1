package com.example.member.service;

import com.example.member.model.Member;
import com.example.member.repository.MemberRepository;
import com.example.member.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // 회원가입
    @Transactional
    public Member register(String name, String userId, String password, String email,
                           LocalDateTime createdAt, String userType) {
        if (memberRepository.existsByUserId(userId)) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }
        if (memberRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        Member member = new Member();
        member.setName(name);
        member.setUserId(userId);
        member.setPassword(passwordEncoder.encode(password));
        member.setEmail(email);
        member.setUserType(userType);
        member.setCreatedAt(createdAt);
        // enum 값을 직접 지정 (예: 회원가입 시 기본값으로 MEMBER 부여)

        // @CreationTimestamp가 있으므로 member.setCreatedAt(createdAt)은 생략해도 자동으로 들어갑니다.
        return memberRepository.save(member);
    }

    // 기존 로그인 (호환성 유지)
    public String login(String userId, String password) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        return jwtUtil.generateToken(member.getUserId(), member.getUserType());
    }

    // 아이디 찾기
    public Member findByNameAndEmail(String name, String email) {
        return memberRepository.findByNameAndEmail(name, email)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
    }

    // 비밀번호 찾기
    public Member findByNameAndUserIdAndEmail(String name, String userId, String email) {
        return memberRepository.findByNameAndUserIdAndEmail(name, userId, email)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
    }

    // 아이디 마스킹
    public String maskUserId(String userId) {
        if (userId == null || userId.length() <= 4) {
            return userId;
        }
        return userId.substring(0, 3) + "***" + userId.substring(userId.length() - 1);
    }

    // 이메일 마스킹
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];
        if (local.length() <= 3) {
            return local.charAt(0) + "***@" + domain;
        }
        return local.substring(0, 3) + "***@" + domain;
    }

    // 임시 비밀번호 생성
    public String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // 비밀번호 업데이트
    @Transactional
    public void updatePassword(Member member, String newPassword) {
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    // 비밀번호 변경 (현재 비밀번호 확인)
    @Transactional
    public void changePassword(String userId, String currentPassword, String newPassword) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        // 소셜 로그인 계정 체크는 제거됨 (Provider 필드 없음)

        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new RuntimeException("INVALID_CURRENT_PASSWORD");
        }

        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    // 프로필 수정
    @Transactional
    public Member updateProfile(String userId, String name, String email) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        if (!member.getEmail().equals(email) && memberRepository.existsByEmail(email)) {
            throw new RuntimeException("DUPLICATE_EMAIL");
        }

        member.setName(name);
        member.setEmail(email);
        return memberRepository.save(member);
    }

    // 중복 체크
    public boolean isUserIdAvailable(String userId) {
        return !memberRepository.existsByUserId(userId);
    }

    public boolean isEmailAvailable(String email) {
        return !memberRepository.existsByEmail(email);
    }

    // 조회
    public Member getMemberByUserId(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
    }

    public Member getMemberByName(String name) {
        return memberRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Optional<Member> getMemberById(String userId) {
        return memberRepository.findById(userId);
    }

    // 소셜 로그인 회원 등록 또는 조회
    @Transactional
    public Member registerOrGetSocialMember(String email, String name, String provider) {
        // 이메일로 기존 회원 조회
        Optional<Member> existingMember = memberRepository.findByEmail(email);
        if (existingMember.isPresent()) {
            return existingMember.get();
        }

        // 신규 회원 등록
        Member member = new Member();
        member.setUserId(provider + "_" + email.split("@")[0]); // 소셜 로그인용 userId 생성
        member.setEmail(email);
        member.setName(name);
        member.setPassword(""); // 소셜 로그인은 비밀번호 불필요
        member.setUserType("member");

        return memberRepository.save(member);
    }

    // 토큰 생성
    public String generateTokenForMember(Member member) {
        return jwtUtil.generateToken(member.getUserId(), member.getUserType());
    }

    // 관리자용
    @Transactional
    public Member updateMember(String userId, String name, String email, String role) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        if (!member.getEmail().equals(email) && memberRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        member.setName(name);
        member.setEmail(email);

        if (role != null && !role.isEmpty()) {
            member.setUserType(role.toLowerCase());
        }

        return memberRepository.save(member);
    }

    // 회원 권한만 변경 (점주 전환 등)
    @Transactional
    public Member updateMemberRole(String userId, String newRole) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        if (newRole != null && !newRole.isEmpty()) {
            member.setUserType(newRole.toLowerCase());
        }

        return memberRepository.save(member);
    }

    @Transactional
    public void deleteMember(String userId) {
        memberRepository.deleteByUserId(userId);
    }

    public boolean verifyUser(String userId, String email) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return member.getEmail().equals(email);
    }

    @Transactional
    public void resetPassword(String userId, String newPassword) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 소셜 로그인 계정 체크는 제거됨 (Provider 필드 없음)

        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }
}
