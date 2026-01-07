package com.du.adminservice.controller;

import com.du.adminservice.model.Member;
import com.du.adminservice.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminUserController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // 회원 목록 조회 (페이징, 검색, 필터) - admin 최상단 고정
    @GetMapping
    public ResponseEntity<?> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String userType) {
        try {
            // URL 디코딩 처리 (한글 검색어 지원)
            String decodedKeyword = null;
            if (keyword != null && !keyword.trim().isEmpty()) {
                decodedKeyword = URLDecoder.decode(keyword.trim(), StandardCharsets.UTF_8);
            }

            System.out.println("[AdminUserController] keyword (원본): " + keyword + ", (디코딩): " + decodedKeyword);

            // 페이지 크기 제한 (최대 50)
            if (size > 50) size = 50;
            if (size < 1) size = 10;

            // 정렬은 쿼리에서 처리 (admin 최상단)
            Pageable pageable = PageRequest.of(page, size);
            Page<Member> memberPage;

            if (userType != null && !userType.trim().isEmpty()) {
                if (decodedKeyword != null && !decodedKeyword.isEmpty()) {
                    memberPage = memberRepository.searchMembersByType(userType, decodedKeyword, pageable);
                } else {
                    memberPage = memberRepository.findByUserTypeOrderByAdminFirst(userType, pageable);
                }
            } else {
                if (decodedKeyword != null && !decodedKeyword.isEmpty()) {
                    memberPage = memberRepository.searchMembers(decodedKeyword, pageable);
                } else {
                    memberPage = memberRepository.findAllOrderByAdminFirst(pageable);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("users", memberPage.getContent());
            response.put("currentPage", memberPage.getNumber());
            response.put("totalPages", memberPage.getTotalPages());
            response.put("totalItems", memberPage.getTotalElements());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("회원 목록 조회 중 오류가 발생했습니다.");
        }
    }

    // 회원 상세 조회
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserDetail(@PathVariable String userId) {
        try {
            Member member = memberRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

            return ResponseEntity.ok(member);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("회원 조회 중 오류가 발생했습니다.");
        }
    }

    // 회원 정보 수정
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable String userId,
            @RequestBody UpdateUserRequest request) {
        try {
            Member member = memberRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                String name = request.getName().trim();
                if (name.length() < 2 || name.length() > 20) {
                    return ResponseEntity.badRequest().body("이름은 2~20자 이내로 입력해주세요.");
                }
                member.setName(name);
            }

            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                String email = request.getEmail().trim();

                // 이메일 형식 검증
                String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
                if (!email.matches(emailRegex)) {
                    return ResponseEntity.badRequest().body("올바른 이메일 형식을 입력해주세요.");
                }

                // 이메일 중복 체크 (자기 자신 제외)
                memberRepository.findByEmail(email)
                        .ifPresent(existingMember -> {
                            if (!existingMember.getUserId().equals(userId)) {
                                throw new RuntimeException("이미 사용 중인 이메일입니다.");
                            }
                        });
                member.setEmail(email);
            }

            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                member.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            memberRepository.save(member);

            Map<String, String> response = new HashMap<>();
            response.put("message", "회원 정보가 수정되었습니다.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("회원 정보 수정 중 오류가 발생했습니다.");
        }
    }

    // 회원 권한 변경
    @PatchMapping("/{userId}/role")
    public ResponseEntity<?> changeUserRole(
            @PathVariable String userId,
            @RequestBody ChangeRoleRequest request) {
        try {
            Member member = memberRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

            // 기본 관리자 계정(admin)은 권한 변경 불가
            if ("admin".equals(userId) && "admin@example.com".equals(member.getEmail())) {
                return ResponseEntity.badRequest().body("기본 관리자 계정의 권한은 변경할 수 없습니다.");
            }

            if (request.getUserType() == null || request.getUserType().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("회원 유형을 입력해주세요.");
            }

            String newUserType = request.getUserType().trim().toLowerCase();
            if (!newUserType.equals("member") && !newUserType.equals("admin") && !newUserType.equals("store_owner")) {
                return ResponseEntity.badRequest().body("유효하지 않은 회원 유형입니다.");
            }

            member.setUserType(newUserType);
            memberRepository.save(member);

            Map<String, String> response = new HashMap<>();
            response.put("message", "회원 권한이 변경되었습니다.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("회원 권한 변경 중 오류가 발생했습니다.");
        }
    }

    // 회원 삭제
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            Member member = memberRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

            // 관리자 계정 삭제 방지
            if ("admin".equalsIgnoreCase(member.getUserType())) {
                return ResponseEntity.badRequest().body("관리자 계정은 삭제할 수 없습니다.");
            }

            memberRepository.delete(member);

            Map<String, String> response = new HashMap<>();
            response.put("message", "회원이 삭제되었습니다.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("회원 삭제 중 오류가 발생했습니다.");
        }
    }

    // DTO 클래스들
    public static class UpdateUserRequest {
        private String name;
        private String email;
        private String password;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class ChangeRoleRequest {
        private String userType;

        public String getUserType() { return userType; }
        public void setUserType(String userType) { this.userType = userType; }
    }
}
