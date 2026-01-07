package com.example.member.controller;

import com.example.member.model.Member;
import com.example.member.service.MemberService;
import com.example.member.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
        
        // ADMIN 또는 STORE_OWNER 권한 확인 (대소문자 무시)
        String role = jwtUtil.getRoleFromToken(token);
        if (!"admin".equalsIgnoreCase(role) && !"store_owner".equalsIgnoreCase(role)) {
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }
        
        return role;
    }

    // 전체 회원 목록 조회 (GET /api/admin/users)
    // - admin.html의 loadUsers()에서 호출
    // - 관리자 페이지에서 회원 테이블 표시용
    // - keyword: 이름 또는 이메일로 검색
    // - userType: 회원 유형 필터 (admin, member, store_owner)
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String userType) {
        try {
            // JWT 토큰 검증 및 ADMIN 권한 확인
            validateAdminToken(authHeader);
            
            // URL 디코딩 처리 (한글 검색어 지원)
            String decodedKeyword = null;
            if (keyword != null && !keyword.trim().isEmpty()) {
                decodedKeyword = URLDecoder.decode(keyword.trim(), StandardCharsets.UTF_8);
            }

            // 디버그 로그
            System.out.println("[AdminController] ========================================");
            System.out.println("[AdminController] keyword (원본): " + keyword);
            System.out.println("[AdminController] keyword (디코딩): " + decodedKeyword);
            System.out.println("[AdminController] userType: " + userType);
            System.out.println("[AdminController] ========================================");

            // 회원 목록 조회 (검색 및 필터 적용)
            List<Member> members;

            if (userType != null && !userType.trim().isEmpty()) {
                // 회원 유형 필터가 있는 경우
                if (decodedKeyword != null && !decodedKeyword.isEmpty()) {
                    System.out.println("[AdminController] Searching by userType AND keyword");
                    members = memberService.searchByUserTypeAndKeyword(userType, decodedKeyword);
                } else {
                    System.out.println("[AdminController] Filtering by userType only");
                    members = memberService.findByUserTypeOrderByAdminFirst(userType);
                }
            } else {
                // 전체 조회
                if (decodedKeyword != null && !decodedKeyword.isEmpty()) {
                    System.out.println("[AdminController] Searching by keyword only");
                    members = memberService.searchByKeyword(decodedKeyword);
                } else {
                    System.out.println("[AdminController] Getting all users");
                    members = memberService.findAllOrderByAdminFirst();
                }
            }

            // 응답 DTO로 변환 (비밀번호 제외)
            List<MemberResponse> users = members.stream()
                    .map(member -> {
                        // Member 엔티티 -> MemberResponse DTO 변환
                        MemberResponse response = new MemberResponse();
                        response.setId(member.getUserId());
                        response.setUsername(member.getName());
                        response.setEmail(member.getEmail());
                        response.setRole(member.getUserType());
                        // createdAt 포맷팅
                        if (member.getCreatedAt() != null) {
                            response.setCreatedAt(member.getCreatedAt().toString());
                        }
                        return response;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            // 권한 없으면 403 응답
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    // 단일 회원 조회 (GET /api/admin/users/{id})
    // - admin.html에서 회원 정보 수정 시 사용
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // JWT 토큰 검증 및 ADMIN 권한 확인
            validateAdminToken(authHeader);

            // 회원 조회
            Member member = memberService.getMemberByUserId(id);

            // 응답 DTO 생성
            MemberResponse response = new MemberResponse();
            response.setId(member.getUserId());
            response.setUsername(member.getName());
            response.setEmail(member.getEmail());
            response.setRole(member.getUserType());
            // createdAt 포맷팅
            if (member.getCreatedAt() != null) {
                response.setCreatedAt(member.getCreatedAt().toString());
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("권한") || e.getMessage().contains("토큰")) {
                return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(404).body(Map.of("error", "사용자를 찾을 수 없습니다."));
        }
    }

    // 회원 정보 수정 (PUT /api/admin/users/{id})
    // - 관리자가 회원의 이름, 이메일, 권한, 비밀번호 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable String id,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // JWT 토큰 검증 및 ADMIN 권한 확인
            validateAdminToken(authHeader);

            // name 또는 username 필드 지원 (호환성)
            String nameValue = request.get("name");
            if (nameValue == null || nameValue.isEmpty()) {
                nameValue = request.get("username");
            }

            // 회원 정보 수정
            Member member = memberService.updateMember(
                    id,
                    nameValue,
                    request.get("email"),
                    request.get("role")
            );

            // 비밀번호가 전달된 경우 비밀번호도 변경
            String newPassword = request.get("password");
            if (newPassword != null && !newPassword.isEmpty()) {
                memberService.resetPassword(id, newPassword);
            }

            // 수정 완료 응답
            Map<String, Object> response = Map.of(
                    "message", "사용자 정보가 업데이트되었습니다.",
                    "id", member.getUserId(),
                    "username", member.getName(),
                    "email", member.getEmail(),
                    "role", member.getUserType()
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
            @PathVariable String id,
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

    // 회원 권한 변경 (PATCH /api/admin/users/{id}/role)
    // - 일반 회원을 점주로 전환하는 등의 권한 변경
    @PatchMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable String id,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // JWT 토큰 검증 및 ADMIN 권한 확인
            validateAdminToken(authHeader);

            // userType 또는 role 필드에서 새로운 권한 추출
            String newRole = request.get("userType");
            if (newRole == null || newRole.isEmpty()) {
                newRole = request.get("role");
            }
            if (newRole == null || newRole.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "변경할 권한을 지정해주세요."));
            }

            // 회원 권한 업데이트 (이름, 이메일은 null로 전달하여 변경하지 않음)
            Member member = memberService.updateMemberRole(id, newRole);

            // 응답 생성
            Map<String, Object> response = Map.of(
                    "message", "권한이 변경되었습니다.",
                    "id", member.getUserId(),
                    "username", member.getName(),
                    "email", member.getEmail(),
                    "role", member.getUserType(),
                    "userType", member.getUserType()
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

    // 응답 DTO 클래스 (비밀번호 제외하고 반환)
    public static class MemberResponse {
        private String id;
        private String userId;
        private String username;
        private String name;
        private String email;
        private String role;
        private String userType;
        private String createdAt;

        public String getId() { return id; }
        public void setId(String id) {
            this.id = id;
            this.userId = id;  // userId도 함께 설정
        }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) {
            this.username = username;
            this.name = username;  // name도 함께 설정
        }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRole() { return role; }
        public void setRole(String role) {
            this.role = role;
            this.userType = role;  // userType도 함께 설정
        }
        public String getUserType() { return userType; }
        public void setUserType(String userType) { this.userType = userType; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}
