package com.example.member.controller;

import com.example.member.model.Member;
import com.example.member.repository.MemberRepository;
import com.example.member.service.EmailService;
import com.example.member.service.MemberService;
import com.example.member.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

// 인증 관련 API 컨트롤러 (회원가입, 로그인, 비밀번호 재설정 등)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    // 생성자 주입 (Spring이 자동으로 호출)
    public AuthController(MemberService memberService, MemberRepository memberRepository, 
                         EmailService emailService, JwtUtil jwtUtil) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
    }

    // 아이디 중복 체크 (GET /api/auth/check-userid?userId=xxx)
    @GetMapping("/check-userid")
    public ResponseEntity<?> checkUserId(@RequestParam String userId) {
        // DB에서 아이디 존재 여부 확인
        boolean exists = memberRepository.existsByUserId(userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    // 이메일 중복 체크 (GET /api/auth/check-email?email=xxx)
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        // DB에서 이메일 존재 여부 확인
        boolean exists = memberRepository.existsByEmail(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    // 이메일 인증코드 발송 (POST /api/auth/send-verification)
    @PostMapping("/send-verification")
    public ResponseEntity<?> sendVerification(@RequestBody EmailRequest request) {
        try {
            // 이메일 유효성 검사
            if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("이메일을 입력해주세요.");
            }
            
            String email = request.getEmail().trim();
            
            // 이메일 중복 체크
            if (memberRepository.existsByEmail(email)) {
                return ResponseEntity.badRequest().body("이미 등록된 이메일입니다.");
            }
            
            // 인증코드 이메일 발송
            emailService.sendVerificationEmail(email);
            Map<String, String> response = new HashMap<>();
            response.put("message", "인증코드가 발송되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // 서버 로그에 에러 출력
            return ResponseEntity.badRequest().body("이메일 발송에 실패했습니다: " + e.getMessage());
        }
    }

    // 인증코드 검증 (POST /api/auth/verify-email)
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyEmailRequest request) {
        // 입력된 인증코드와 저장된 코드 비교
        boolean verified = emailService.verifyCode(request.getEmail(), request.getCode());
        Map<String, Object> response = new HashMap<>();
        response.put("verified", verified);
        
        // 인증 실패 시 메시지 추가
        if (!verified) {
            response.put("message", "인증코드가 일치하지 않거나 만료되었습니다.");
        }
        return ResponseEntity.ok(response);
    }

    // 회원가입 (POST /api/auth/register)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // 회원 등록
            Member member = memberService.register(
                    request.getUsername(),
                    request.getUserId(),
                    request.getPassword(),
                    request.getEmail(),
                    request.getBirthDate(),
                    request.getPhoneNum()
            );

            Map<String, String> response = new HashMap<>();
            response.put("message", "회원 가입이 완료되었습니다.");
            response.put("username", member.getUsername());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 로그인 (POST /api/auth/login)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // 로그인 검증 후 JWT 토큰 생성
            String token = memberService.login(request.getUserId(), request.getPassword());
            
            // 회원 정보 조회
            Member member = memberService.getMemberByUserId(request.getUserId());

            // 토큰과 회원 정보 반환
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("username", member.getUsername());
            response.put("userId", member.getUserId());
            response.put("role", member.getRole().name());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 본인 확인 (POST /api/auth/verify-user)
    // - 비밀번호 재설정 전 아이디+이메일로 본인 확인
    @PostMapping("/verify-user")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyRequest request) {
        try {
            // 아이디와 이메일이 일치하는지 확인
            boolean verified = memberService.verifyUser(request.getUserId(), request.getEmail());
            
            if (verified) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "본인 확인이 완료되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body("사용자 정보가 일치하지 않습니다.");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 비밀번호 재설정 (POST /api/auth/reset-password)
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            // 새 비밀번호로 변경
            memberService.resetPassword(request.getUserId(), request.getNewPassword());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "비밀번호가 변경되었습니다.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========== DTO 클래스 ==========

    // 회원가입 요청 DTO
    public static class RegisterRequest {
        private String username;
        private String userId;
        private String password;
        private String email;
        private LocalDate birthDate;
        private String phoneNum;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public LocalDate getBirthDate() { return birthDate; }
        public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
        public String getPhoneNum() { return phoneNum; }
        public void setPhoneNum(String phoneNum) { this.phoneNum = phoneNum; }
    }

    // 로그인 요청 DTO
    public static class LoginRequest {
        private String userId;
        private String password;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // 본인 확인 요청 DTO
    public static class VerifyRequest {
        private String userId;
        private String email;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // 비밀번호 재설정 요청 DTO
    public static class ResetPasswordRequest {
        private String userId;
        private String newPassword;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    // 이메일 인증코드 발송 요청 DTO
    public static class EmailRequest {
        private String email;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // 이메일 인증코드 검증 요청 DTO
    public static class VerifyEmailRequest {
        private String email;
        private String code;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
}
