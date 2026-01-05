package com.example.member.controller;

import com.example.member.dto.request.RegisterRequest;
import com.example.member.model.Member;
import com.example.member.repository.MemberRepository;
import com.example.member.service.EmailService;
import com.example.member.service.MemberService;
import com.example.member.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(MemberService memberService, MemberRepository memberRepository,
                          EmailService emailService, JwtUtil jwtUtil) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // ==================== 회원가입 ====================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // 이름 유효성 검사 (2~20자, 한글/영문)
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "이름을 입력해주세요.");
                error.put("errorCode", "BAD_REQUEST");
                return ResponseEntity.badRequest().body(error);
            }

            if (!isValidName(request.getName().trim())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "이름은 2~20자의 한글 또는 영문만 가능합니다.");
                error.put("errorCode", "INVALID_NAME");
                return ResponseEntity.badRequest().body(error);
            }

            // 아이디 유효성 검사 (4~20자, 영문 소문자+숫자)
            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "아이디를 입력해주세요.");
                error.put("errorCode", "BAD_REQUEST");
                return ResponseEntity.badRequest().body(error);
            }

            if (!isValidUserId(request.getUserId().trim())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "아이디는 4~20자의 영문 소문자, 숫자만 가능합니다.");
                error.put("errorCode", "INVALID_USER_ID");
                return ResponseEntity.badRequest().body(error);
            }

            // 이메일 유효성 검사
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "이메일을 입력해주세요.");
                error.put("errorCode", "BAD_REQUEST");
                return ResponseEntity.badRequest().body(error);
            }

            if (!isValidEmail(request.getEmail().trim())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "올바른 이메일 형식이 아닙니다.");
                error.put("errorCode", "INVALID_EMAIL");
                return ResponseEntity.badRequest().body(error);
            }

            // 비밀번호 유효성 검사
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "비밀번호를 입력해주세요.");
                error.put("errorCode", "BAD_REQUEST");
                return ResponseEntity.badRequest().body(error);
            }

            // 비밀번호 강도 검증 (8~20자, 영문/숫자/특수문자 중 2가지 이상)
            if (!isValidPassword(request.getPassword())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "비밀번호는 8~20자, 영문/숫자/특수문자 중 2가지 이상 조합이어야 합니다.");
                error.put("errorCode", "WEAK_PASSWORD");
                return ResponseEntity.badRequest().body(error);
            }

            if (!request.getPassword().equals(request.getPasswordConfirm())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "비밀번호가 일치하지 않습니다.");
                error.put("errorCode", "PASSWORD_MISMATCH");
                return ResponseEntity.badRequest().body(error);
            }

            // 중복 체크
            if (memberRepository.existsByUserId(request.getUserId())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "이미 사용 중인 아이디입니다.");
                error.put("errorCode", "DUPLICATE_USER_ID");
                return ResponseEntity.status(409).body(error);
            }

            if (memberRepository.existsByEmail(request.getEmail())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "이미 사용 중인 이메일입니다.");
                error.put("errorCode", "DUPLICATE_EMAIL");
                return ResponseEntity.status(409).body(error);
            }

            // Member 엔티티 생성
            Member member = new Member();
            member.setName(request.getName().trim());
            member.setUserId(request.getUserId().trim());
            member.setPassword(passwordEncoder.encode(request.getPassword()));
            member.setEmail(request.getEmail().trim());
            member.setUserType("member");

            // 저장
            Member saved = memberRepository.save(member);

            // 성공 응답 (비밀번호 제외)
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("id", saved.getUserId());
            responseData.put("userId", saved.getUserId());
            responseData.put("name", saved.getName());
            responseData.put("email", saved.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "회원가입이 완료되었습니다.");
            response.put("data", responseData);

            return ResponseEntity.status(201).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            error.put("errorCode", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(500).body(error);
        }
    }

    // ==================== 이메일 인증 ====================
    @PostMapping("/email/send-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String purpose = request.get("purpose");

            if (email == null || email.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "이메일을 입력해주세요.");
                error.put("errorCode", "BAD_REQUEST");
                return ResponseEntity.badRequest().body(error);
            }

            email = email.trim();

            // 회원가입용: 이메일 중복 체크
            if ("register".equals(purpose) && memberRepository.existsByEmail(email)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "이미 등록된 이메일입니다.");
                error.put("errorCode", "DUPLICATE_EMAIL");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }

            // 재발송 제한 체크
            Long retryAfter = emailService.checkResendLimit(email);
            if (retryAfter != null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "인증번호는 3분마다 발송 가능합니다.");
                error.put("errorCode", "TOO_MANY_REQUESTS");
                error.put("retryAfter", retryAfter);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
            }

            // 인증코드 발송
            emailService.sendVerificationEmail(email);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "인증번호가 발송되었습니다.");
            
            Map<String, Object> data = new HashMap<>();
            data.put("email", email);
            data.put("expiresIn", 180);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "이메일 발송에 실패했습니다: " + e.getMessage());
            error.put("errorCode", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/email/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = request.get("verificationCode");

            String result = emailService.verifyCode(email, code);

            switch (result) {
                case "SUCCESS":
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "인증이 완료되었습니다.");

                    Map<String, Boolean> data = new HashMap<>();
                    data.put("verified", true);
                    response.put("data", data);

                    return ResponseEntity.ok(response);

                case "EXPIRED":
                    Map<String, Object> expiredError = new HashMap<>();
                    expiredError.put("success", false);
                    expiredError.put("message", "인증번호가 만료되었습니다. 다시 발송해주세요.");
                    expiredError.put("errorCode", "VERIFICATION_CODE_EXPIRED");
                    return ResponseEntity.status(HttpStatus.GONE).body(expiredError);

                case "NOT_FOUND":
                    Map<String, Object> notFoundError = new HashMap<>();
                    notFoundError.put("success", false);
                    notFoundError.put("message", "인증번호를 먼저 발송해주세요.");
                    notFoundError.put("errorCode", "VERIFICATION_CODE_NOT_FOUND");
                    return ResponseEntity.badRequest().body(notFoundError);

                default: // INVALID
                    Map<String, Object> invalidError = new HashMap<>();
                    invalidError.put("success", false);
                    invalidError.put("message", "인증번호가 일치하지 않습니다.");
                    invalidError.put("errorCode", "INVALID_VERIFICATION_CODE");
                    return ResponseEntity.badRequest().body(invalidError);
            }

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "서버 오류가 발생했습니다.");
            error.put("errorCode", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== 아이디 중복 확인 ====================
    @GetMapping("/check-userid")
    public ResponseEntity<?> checkUserId(@RequestParam String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "아이디를 입력해주세요.");
                error.put("errorCode", "BAD_REQUEST");
                return ResponseEntity.badRequest().body(error);
            }

            boolean available = !memberRepository.existsByUserId(userId.trim());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", available ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다.");

            Map<String, Boolean> data = new HashMap<>();
            data.put("available", available);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "서버 오류가 발생했습니다.");
            error.put("errorCode", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== 로그인 ====================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String password = request.get("password");
            String userType = request.get("userType"); // "normal" 또는 "admin"

            if (userId == null || userId.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "아이디를 입력해주세요.");
                error.put("errorCode", "BAD_REQUEST");
                return ResponseEntity.badRequest().body(error);
            }

            if (password == null || password.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "비밀번호를 입력해주세요.");
                error.put("errorCode", "BAD_REQUEST");
                return ResponseEntity.badRequest().body(error);
            }

            // 회원 조회 (아이디 또는 이메일로 로그인)
            Member member = null;
            String trimmedUserId = userId.trim();

            // 이메일 형식인지 확인
            if (trimmedUserId.contains("@")) {
                // 이메일로 조회
                member = memberRepository.findByEmail(trimmedUserId).orElse(null);
            } else {
                // 아이디로 조회
                member = memberRepository.findByUserId(trimmedUserId).orElse(null);
            }

            if (member == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "아이디 또는 비밀번호가 올바르지 않습니다.");
                error.put("errorCode", "AUTH_FAILED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // 계정 잠금 상태 확인
            if (member.isAccountLocked()) {
                long remainingMinutes = java.time.Duration.between(
                        java.time.LocalDateTime.now(),
                        member.getAccountLockedUntil()).toMinutes() + 1;
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "계정이 잠겨있습니다. " + remainingMinutes + "분 후에 다시 시도해주세요.");
                error.put("errorCode", "ACCOUNT_LOCKED");
                error.put("remainingMinutes", remainingMinutes);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // 비밀번호 검증
            if (!passwordEncoder.matches(password, member.getPassword())) {
                // 로그인 실패 기록
                member.recordLoginFailure();
                memberRepository.save(member);

                int remainingAttempts = 5 - member.getFailedLoginAttempts();
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                if (member.isAccountLocked()) {
                    error.put("message", "비밀번호 5회 오류로 계정이 30분간 잠겼습니다.");
                    error.put("errorCode", "ACCOUNT_LOCKED");
                } else {
                    error.put("message", "비밀번호가 올바르지 않습니다. (남은 시도 횟수: " + remainingAttempts + "회)");
                    error.put("errorCode", "AUTH_FAILED");
                    error.put("remainingAttempts", remainingAttempts);
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // 로그인 타입과 회원 유형 검증
            String memberUserType = member.getUserType() != null ? member.getUserType().toLowerCase() : "member";

            // 관리자 로그인 시
            if ("admin".equals(userType)) {
                // 관리자 또는 점주만 관리자 로그인 가능
                if (!"admin".equals(memberUserType) && !"store_owner".equals(memberUserType)) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "관리자 권한이 없습니다.");
                    error.put("errorCode", "AUTH_FAILED");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            } else {
                // 일반 로그인 시 - 점주나 관리자는 일반 로그인 불가
                if ("admin".equals(memberUserType) || "store_owner".equals(memberUserType)) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "관리자 로그인을 이용해주세요.");
                    error.put("errorCode", "AUTH_FAILED");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            }

            // 로그인 성공 - 실패 횟수 초기화
            if (member.getFailedLoginAttempts() > 0) {
                member.resetLoginFailures();
                memberRepository.save(member);
            }

            // JWT 토큰 생성
            String accessToken = jwtUtil.generateAccessToken(member.getUserId(), member.getUserType());
            String refreshToken = jwtUtil.generateRefreshToken(member.getUserId());

            // 성공 응답 - login.html이 기대하는 형식 (accessToken, username, userId, role)
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "로그인 성공");
            response.put("accessToken", accessToken);  // frontend는 accessToken 필드명 기대
            response.put("token", accessToken);  // 하위 호환성을 위해 token도 포함
            response.put("refreshToken", refreshToken);
            response.put("userId", member.getUserId());
            response.put("username", member.getName());
            response.put("email", member.getEmail());
            response.put("role", member.getUserType().toUpperCase());  // 대문자로 변환

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "서버 오류가 발생했습니다.");
            error.put("errorCode", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== 아이디 찾기 ====================
    @PostMapping("/find-userid")
    public ResponseEntity<?> findUserId(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String email = request.get("email");

            if (name == null || name.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "이름을 입력해주세요.");
                error.put("errorCode", "BAD_REQUEST");
                return ResponseEntity.badRequest().body(error);
            }

            if (email == null || email.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "이메일을 입력해주세요.");
                error.put("errorCode", "BAD_REQUEST");
                return ResponseEntity.badRequest().body(error);
            }

            // 회원 조회
            Member member = memberRepository.findByNameAndEmail(name.trim(), email.trim()).orElse(null);
            if (member == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "일치하는 회원 정보를 찾을 수 없습니다.");
                error.put("errorCode", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // 아이디 마스킹 처리
            String userId = member.getUserId();
            String maskedUserId = maskUserId(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "아이디를 찾았습니다.");

            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("maskedUserId", maskedUserId);
            data.put("createdAt", member.getCreatedAt() != null ? member.getCreatedAt().toString() : null);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "서버 오류가 발생했습니다.");
            error.put("errorCode", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== 사용자 본인 확인 (비밀번호 찾기 1단계) ====================
    @PostMapping("/verify-user")
    public ResponseEntity<?> verifyUser(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String email = request.get("email");

            if (userId == null || userId.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "아이디를 입력해주세요.");
                error.put("errorCode", "BAD_REQUEST");
                return ResponseEntity.badRequest().body(error);
            }

            if (email == null || email.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "이메일을 입력해주세요.");
                error.put("errorCode", "BAD_REQUEST");
                return ResponseEntity.badRequest().body(error);
            }

            // 회원 조회 (userId와 email로만 확인)
            Member member = memberRepository.findByUserIdAndEmail(
                    userId.trim(), email.trim()).orElse(null);
            if (member == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "가입된 정보가 없습니다.");
                error.put("errorCode", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // 본인 확인 성공
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "본인 확인이 완료되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            error.put("errorCode", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== 비밀번호 찾기 (임시 비밀번호 발급) ====================
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String userId = request.get("userId");
            String email = request.get("email");

            if (name == null || name.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "이름을 입력해주세요.");
                error.put("errorCode", "BAD_REQUEST");
                return ResponseEntity.badRequest().body(error);
            }

            if (userId == null || userId.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "아이디를 입력해주세요.");
                error.put("errorCode", "BAD_REQUEST");
                return ResponseEntity.badRequest().body(error);
            }

            if (email == null || email.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "이메일을 입력해주세요.");
                error.put("errorCode", "BAD_REQUEST");
                return ResponseEntity.badRequest().body(error);
            }

            // 회원 조회
            Member member = memberRepository.findByNameAndUserIdAndEmail(
                    name.trim(), userId.trim(), email.trim()).orElse(null);
            if (member == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "일치하는 회원 정보를 찾을 수 없습니다.");
                error.put("errorCode", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // 소셜 로그인 계정 체크는 제거됨 (Provider 필드 없음)

            // 임시 비밀번호 생성
            String tempPassword = memberService.generateTempPassword();

            // 비밀번호 업데이트
            member.setPassword(passwordEncoder.encode(tempPassword));
            memberRepository.save(member);

            // 임시 비밀번호 이메일 발송
            emailService.sendTempPasswordEmail(email.trim(), tempPassword);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "임시 비밀번호가 이메일로 발송되었습니다.");

            Map<String, Object> data = new HashMap<>();
            data.put("email", maskEmail(email.trim()));
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            error.put("errorCode", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== 비밀번호 변경 ====================
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        try {
            // 토큰에서 userId 추출
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "인증이 필요합니다.");
                error.put("errorCode", "UNAUTHORIZED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String token = authHeader.substring(7);
            String userId = jwtUtil.extractUserId(token);

            if (userId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "유효하지 않은 토큰입니다.");
                error.put("errorCode", "INVALID_TOKEN");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            String newPasswordConfirm = request.get("newPasswordConfirm");

            // 유효성 검사
            if (currentPassword == null || currentPassword.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "현재 비밀번호를 입력해주세요.");
                error.put("errorCode", "BAD_REQUEST");
                return ResponseEntity.badRequest().body(error);
            }

            if (newPassword == null || newPassword.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "새 비밀번호를 입력해주세요.");
                error.put("errorCode", "BAD_REQUEST");
                return ResponseEntity.badRequest().body(error);
            }

            if (!newPassword.equals(newPasswordConfirm)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "새 비밀번호가 일치하지 않습니다.");
                error.put("errorCode", "PASSWORD_MISMATCH");
                return ResponseEntity.badRequest().body(error);
            }

            // 비밀번호 강도 검증
            if (!isValidPassword(newPassword)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "비밀번호는 8자 이상, 영문/숫자/특수문자를 포함해야 합니다.");
                error.put("errorCode", "WEAK_PASSWORD");
                return ResponseEntity.badRequest().body(error);
            }

            // 비밀번호 변경
            memberService.changePassword(userId, currentPassword, newPassword);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "비밀번호가 변경되었습니다.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            if ("INVALID_CURRENT_PASSWORD".equals(e.getMessage())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "현재 비밀번호가 일치하지 않습니다.");
                error.put("errorCode", "INVALID_CURRENT_PASSWORD");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            if ("USER_NOT_FOUND".equals(e.getMessage())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "사용자를 찾을 수 없습니다.");
                error.put("errorCode", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "서버 오류가 발생했습니다.");
            error.put("errorCode", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== 관리자용 비밀번호 초기화 ====================
    @PutMapping("/admin/reset-password")
    public ResponseEntity<?> adminResetPassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        try {
            // 토큰에서 userId와 권한 추출
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "인증이 필요합니다.");
                error.put("errorCode", "UNAUTHORIZED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String token = authHeader.substring(7);
            String adminUserId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            // 관리자 권한 체크
            if (!"admin".equalsIgnoreCase(role) && !"store_owner".equalsIgnoreCase(role)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "관리자 권한이 필요합니다.");
                error.put("errorCode", "FORBIDDEN");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            String targetUserId = request.get("userId");
            String newPassword = request.get("newPassword");

            if (targetUserId == null || targetUserId.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "대상 사용자 아이디를 입력해주세요.");
                error.put("errorCode", "BAD_REQUEST");
                return ResponseEntity.badRequest().body(error);
            }

            if (newPassword == null || newPassword.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "새 비밀번호를 입력해주세요.");
                error.put("errorCode", "BAD_REQUEST");
                return ResponseEntity.badRequest().body(error);
            }

            // 비밀번호 강도 검증
            if (!isValidPassword(newPassword)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "비밀번호는 8자 이상, 영문/숫자/특수문자를 포함해야 합니다.");
                error.put("errorCode", "WEAK_PASSWORD");
                return ResponseEntity.badRequest().body(error);
            }

            // 비밀번호 초기화
            memberService.resetPassword(targetUserId.trim(), newPassword);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "비밀번호가 초기화되었습니다.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("찾을 수 없습니다")) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "사용자를 찾을 수 없습니다.");
                error.put("errorCode", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "서버 오류가 발생했습니다.");
            error.put("errorCode", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== 토큰 갱신 ====================
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");

            if (refreshToken == null || refreshToken.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Refresh Token이 필요합니다.");
                error.put("errorCode", "BAD_REQUEST");
                return ResponseEntity.badRequest().body(error);
            }

            // Refresh Token 유효성 검증
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Refresh Token이 만료되었습니다. 다시 로그인해주세요.");
                error.put("errorCode", "REFRESH_TOKEN_EXPIRED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // 토큰에서 userId 추출
            String userId = jwtUtil.getUserIdFromToken(refreshToken);
            Member member = memberRepository.findByUserId(userId).orElse(null);

            if (member == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "사용자를 찾을 수 없습니다.");
                error.put("errorCode", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // 새로운 토큰 발급
            String newAccessToken = jwtUtil.generateAccessToken(userId, member.getUserType());
            String newRefreshToken = jwtUtil.generateRefreshToken(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "토큰이 갱신되었습니다.");

            Map<String, String> data = new HashMap<>();
            data.put("accessToken", newAccessToken);
            data.put("refreshToken", newRefreshToken);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "서버 오류가 발생했습니다.");
            error.put("errorCode", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== 로그아웃 ====================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            // Access Token 블랙리스트 추가
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String accessToken = authHeader.substring(7);
                jwtUtil.addToBlacklist(accessToken);
            }

            // Refresh Token 블랙리스트 추가
            if (request != null && request.get("refreshToken") != null) {
                String refreshToken = request.get("refreshToken");
                jwtUtil.addToBlacklist(refreshToken);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "로그아웃 되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "서버 오류가 발생했습니다.");
            error.put("errorCode", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== 유틸리티 메서드 ====================
    private String maskUserId(String userId) {
        if (userId == null || userId.length() <= 4) {
            return userId;
        }
        return userId.substring(0, 3) + "***" + userId.substring(userId.length() - 1);
    }

    private String maskEmail(String email) {
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

    // 비밀번호 강도 검증 (8~20자, 영문/숫자/특수문자 중 2가지 이상 조합)
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 20) {
            return false;
        }
        // 영문, 숫자, 특수문자 중 2가지 이상 조합
        boolean hasLetter = password.matches(".*[A-Za-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*].*");

        int count = 0;
        if (hasLetter) count++;
        if (hasDigit) count++;
        if (hasSpecial) count++;

        return count >= 2;
    }

    // 아이디 검증 (4~20자, 영문 소문자+숫자 조합)
    private boolean isValidUserId(String userId) {
        if (userId == null) {
            return false;
        }
        return userId.matches("^[a-z0-9]{4,20}$");
    }

    // 이메일 검증
    private boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    // 이름 검증 (2~20자, 한글/영문)
    private boolean isValidName(String name) {
        if (name == null) {
            return false;
        }
        return name.matches("^[가-힣a-zA-Z]{2,20}$");
    }
}
