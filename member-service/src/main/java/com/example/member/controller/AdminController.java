package com.example.member.controller;

import com.example.member.model.Member;
import com.example.member.service.MemberService;
import com.example.member.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 관리자 전용 회원 관리 API 컨트롤러
@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    // 생성자 주입 (Spring이 자동으로 호출하여 의존성 주입)
    public AdminController(MemberService memberService, JwtUtil jwtUtil) {
        this.memberService = memberService;
        this.jwtUtil = jwtUtil;
    }

    // JWT 토큰 검증 헬퍼 메서드
    // - 실패 시 RuntimeException 발생
    private String validateAdminToken(String authHeader) {
        // Bearer 토큰 존재 여부 확인
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("인증 토큰이 없습니다.");
        }
        
        // "Bearer " 제거하고 토큰만 추출
        String token = authHeader.substring(7);
        
        // 토큰 유효성 검증
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }
        
        // ADMIN 권한 확인
        String role = jwtUtil.getRoleFromToken(token);
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }
        
        return role;
    }

    // 전체 회원 목록 조회 (GET /api/admin/users)
    // - admin.html의 loadUsers()에서 호출
    // - 관리자 페이지에서 회원 테이블 표시용
    @GetMapping
    public ResponseEntity<?> getAllUsers(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // JWT 토큰 검증 및 ADMIN 권한 확인
            validateAdminToken(authHeader);
            
            // 전체 회원 조회 후 응답 DTO로 변환 (비밀번호 제외)
            List<MemberResponse> users = memberService.getAllMembers().stream()
                    .map(member -> {
                        // Member 엔티티 -> MemberResponse DTO 변환
                        MemberResponse response = new MemberResponse();
                        response.setId(member.getId());
                        response.setUsername(member.getUsername());
                        response.setEmail(member.getEmail());
                        response.setRole(member.getRole().name());
                        return response;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            // 권한 없으면 403 응답
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    // 회원 정보 수정 (PUT /api/admin/users/{id})
    // - 관리자가 회원의 이름, 이메일, 권한 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id, 
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // JWT 토큰 검증 및 ADMIN 권한 확인
            validateAdminToken(authHeader);
            
            // 회원 정보 수정
            Member member = memberService.updateMember(
                    id,
                    request.get("username"),
                    request.get("email"),
                    request.get("role")
            );

            // 수정 완료 응답
            Map<String, Object> response = Map.of(
                    "message", "사용자 정보가 업데이트되었습니다.",
                    "id", member.getId(),
                    "username", member.getUsername(),
                    "email", member.getEmail(),
                    "role", member.getRole().name()
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // 권한/토큰 관련 에러는 403, 나머지는 400 응답
            if (e.getMessage().contains("권한") || e.getMessage().contains("토큰")) {
                return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 회원 삭제 (DELETE /api/admin/users/{id})
    // - 관리자가 회원 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // JWT 토큰 검증 및 ADMIN 권한 확인
            validateAdminToken(authHeader);
            
            // 회원 삭제
            memberService.deleteMember(id);
            return ResponseEntity.ok(Map.of("message", "사용자가 삭제되었습니다."));
        } catch (RuntimeException e) {
            // 권한/토큰 관련 에러는 403, 나머지는 400 응답
            if (e.getMessage().contains("권한") || e.getMessage().contains("토큰")) {
                return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 응답 DTO 클래스 (비밀번호 제외하고 반환)
    public static class MemberResponse {
        private Long id;
        private String username;
        private String email;
        private String role;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
