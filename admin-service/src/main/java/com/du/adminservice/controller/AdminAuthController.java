package com.du.adminservice.controller;

import com.du.adminservice.model.Member;
import com.du.adminservice.repository.MemberRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String secretKey;

    public AdminAuthController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("아이디를 입력해주세요.");
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("비밀번호를 입력해주세요.");
            }

            Member member = memberRepository.findByUserId(request.getUserId().trim())
                    .orElse(null);

            if (member == null) {
                return ResponseEntity.status(401).body("아이디 또는 비밀번호가 일치하지 않습니다.");
            }

            if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
                return ResponseEntity.status(401).body("아이디 또는 비밀번호가 일치하지 않습니다.");
            }

            String memberUserType = member.getUserType() != null ? member.getUserType().toLowerCase() : "";
            if (!"admin".equals(memberUserType)) {
                return ResponseEntity.status(403).body("관리자만 접근할 수 있습니다.");
            }

            String accessToken = generateToken(member.getUserId(), member.getUserType(), 3600000);
            String refreshToken = generateToken(member.getUserId(), member.getUserType(), 86400000);

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("userId", member.getUserId());
            response.put("userName", member.getName());
            response.put("userRole", "ADMIN");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("로그인 중 오류가 발생했습니다.");
        }
    }

    private String generateToken(String userId, String userType, long expiration) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", userType.toUpperCase())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();
    }

    public static class LoginRequest {
        private String userId;
        private String password;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
