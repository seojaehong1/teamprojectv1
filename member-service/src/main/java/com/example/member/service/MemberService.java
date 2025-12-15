package com.example.member.service;

import com.example.member.model.Member;
import com.example.member.repository.MemberRepository;
import com.example.member.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// 회원 관련 비즈니스 로직 서비스
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public MemberService(MemberRepository memberRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // 기본 회원가입 (DEFAULT)
    @Transactional
    public Member register(String username, String userId, String password, String email, 
                          LocalDate birthDate, String phoneNum) {
        // 아이디 중복 체크
        if (memberRepository.existsByUserId(userId)) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        Member member = new Member();
        member.setUsername(username);
        member.setUserId(userId);
        member.setPassword(passwordEncoder.encode(password));
        member.setEmail(email);
        member.setBirthDate(birthDate);
        member.setPhoneNum(phoneNum);
        member.setProvider(Member.Provider.DEFAULT);
        member.setRole(Member.Role.USER);

        return memberRepository.save(member);
    }

    // 소셜 로그인용 회원가입/조회 (password, birthDate, phoneNum은 NULL)
    @Transactional
    public Member registerOrGetSocialMember(String email, String username, Member.Provider provider) {
        Optional<Member> existing = memberRepository.findByEmail(email);
        
        // 기존 회원이면 반환
        if (existing.isPresent()) {
            return existing.get();
        }

        // 신규 소셜 회원 생성
        Member member = new Member();
        member.setUsername(username);
        member.setUserId(provider.name().toLowerCase() + "_" + System.currentTimeMillis());
        member.setEmail(email);
        member.setProvider(provider);
        member.setRole(Member.Role.USER);
        // 소셜 로그인은 password, birthDate, phoneNum을 NULL로 명시
        member.setPassword(null);
        member.setBirthDate(null);
        member.setPhoneNum(null);

        return memberRepository.save(member);
    }

    // 일반 로그인
    public String login(String userId, String password) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다."));

        // 소셜 로그인 계정은 일반 로그인 불가
        if (member.getProvider() != Member.Provider.DEFAULT) {
            throw new RuntimeException("소셜 로그인으로 가입된 계정입니다. " + member.getProvider() + " 로그인을 이용해주세요.");
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        return jwtUtil.generateToken(member.getUserId(), member.getRole().name());
    }

    // 회원 정보로 JWT 토큰 생성
    public String generateTokenForMember(Member member) {
        return jwtUtil.generateToken(member.getUserId(), member.getRole().name());
    }

    // username으로 회원 조회
    public Member getMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    // userId로 회원 조회
    public Member getMemberByUserId(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    // email로 회원 조회
    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    // 전체 회원 목록 조회
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    // ID로 회원 조회
    public Optional<Member> getMemberById(Long id) {
        return memberRepository.findById(id);
    }

    // 회원 정보 수정
    @Transactional
    public Member updateMember(Long id, String username, String email, String role) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        // 이메일 중복 체크 (본인 제외)
        if (!member.getEmail().equals(email) && memberRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        member.setUsername(username);
        member.setEmail(email);
        
        // 권한 변경
        if (role != null && !role.isEmpty()) {
            try {
                member.setRole(Member.Role.valueOf(role));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("유효하지 않은 역할입니다: " + role);
            }
        }

        return memberRepository.save(member);
    }

    // 회원 삭제
    @Transactional
    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    // 사용자 본인 인증 (아이디 + 이메일)
    public boolean verifyUser(String userId, String email) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return member.getEmail().equals(email);
    }

    // 비밀번호 재설정
    @Transactional
    public void resetPassword(String userId, String newPassword) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 소셜 로그인 계정은 비밀번호 변경 불가
        if (member.getProvider() != Member.Provider.DEFAULT) {
            throw new RuntimeException("소셜 로그인 계정은 비밀번호를 변경할 수 없습니다.");
        }
        
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }
}
