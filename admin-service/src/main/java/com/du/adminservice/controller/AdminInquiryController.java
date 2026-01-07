package com.du.adminservice.controller;

import com.du.adminservice.model.Inquiry;
import com.du.adminservice.repository.InquiryRepository;
import com.du.adminservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/inquiries")
public class AdminInquiryController {

    private final InquiryRepository inquiryRepository;
    private final JwtUtil jwtUtil;

    public AdminInquiryController(InquiryRepository inquiryRepository, JwtUtil jwtUtil) {
        this.inquiryRepository = inquiryRepository;
        this.jwtUtil = jwtUtil;
    }

    // 관리자 권한 검증 헬퍼 메서드
    private void validateAdminRole(HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (role == null || (!"admin".equalsIgnoreCase(role) && !"store_owner".equalsIgnoreCase(role))) {
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }
    }

    // 문의 목록 조회 (페이징, 검색, 필터)
    @GetMapping
    public ResponseEntity<?> getInquiryList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            HttpServletRequest httpRequest) {
        try {
            validateAdminRole(httpRequest);

            // URL 디코딩 처리 (한글 검색어 지원)
            String decodedKeyword = null;
            if (keyword != null && !keyword.trim().isEmpty()) {
                decodedKeyword = URLDecoder.decode(keyword.trim(), StandardCharsets.UTF_8);
            }

            // 디버그 로그
            System.out.println("[AdminInquiryController] ========================================");
            System.out.println("[AdminInquiryController] keyword (원본): " + keyword);
            System.out.println("[AdminInquiryController] keyword (디코딩): " + decodedKeyword);
            System.out.println("[AdminInquiryController] status: " + status);
            System.out.println("[AdminInquiryController] page: " + page + ", size: " + size);
            System.out.println("[AdminInquiryController] ========================================");

            // 페이지 크기 제한 (최대 50)
            if (size > 50) size = 50;
            if (size < 1) size = 10;

            Pageable pageable = PageRequest.of(page, size);
            Page<Inquiry> inquiryPage;

            if (status != null && !status.trim().isEmpty()) {
                // 상태 필터가 있으면 해당 상태만 조회 (작성일 내림차순)
                if (decodedKeyword != null && !decodedKeyword.isEmpty()) {
                    System.out.println("[AdminInquiryController] Searching by status AND keyword");
                    inquiryPage = inquiryRepository.searchInquiriesByStatus(status.toUpperCase(), decodedKeyword, pageable);
                } else {
                    System.out.println("[AdminInquiryController] Filtering by status only");
                    inquiryPage = inquiryRepository.findByStatus(status.toUpperCase(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending()));
                }
            } else {
                // 전체 조회: 답변대기 우선, 작성일 내림차순
                if (decodedKeyword != null && !decodedKeyword.isEmpty()) {
                    System.out.println("[AdminInquiryController] Searching by keyword only");
                    inquiryPage = inquiryRepository.searchInquiries(decodedKeyword, pageable);
                } else {
                    System.out.println("[AdminInquiryController] Getting all inquiries");
                    inquiryPage = inquiryRepository.findAllOrderByStatusAndCreatedAt(pageable);
                }
            }

            System.out.println("[AdminInquiryController] Found " + inquiryPage.getTotalElements() + " inquiries");

            Map<String, Object> response = new HashMap<>();
            response.put("inquiries", inquiryPage.getContent());
            response.put("currentPage", inquiryPage.getNumber());
            response.put("totalPages", inquiryPage.getTotalPages());
            response.put("totalItems", inquiryPage.getTotalElements());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("권한")) {
                return ResponseEntity.status(403).body(e.getMessage());
            }
            return ResponseEntity.status(500).body("문의 목록 조회 중 오류가 발생했습니다.");
        }
    }

    // 문의 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> getInquiryDetail(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            validateAdminRole(httpRequest);
            Inquiry inquiry = inquiryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

            return ResponseEntity.ok(inquiry);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("권한")) {
                return ResponseEntity.status(403).body(e.getMessage());
            }
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("문의 조회 중 오류가 발생했습니다.");
        }
    }

    // 문의 답변 등록/수정
    @PostMapping("/{id}/answer")
    @Transactional
    public ResponseEntity<?> answerInquiry(
            @PathVariable Long id,
            @RequestBody AnswerRequest request,
            HttpServletRequest httpRequest) {
        try {
            validateAdminRole(httpRequest);
            String userId = (String) httpRequest.getAttribute("userId");

            Inquiry inquiry = inquiryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

            if (request.getAnswer() == null || request.getAnswer().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("답변 내용을 입력해주세요.");
            }

            String answerContent = request.getAnswer().trim();
            if (answerContent.length() < 10) {
                return ResponseEntity.badRequest().body("답변은 10자 이상 입력해주세요.");
            }

            if (answerContent.length() > 2000) {
                return ResponseEntity.badRequest().body("답변은 2000자 이내로 입력해주세요.");
            }

            inquiry.setAnswer(request.getAnswer().trim());
            inquiry.setAnsweredBy(userId);
            inquiry.setAnsweredAt(LocalDateTime.now());
            inquiry.setStatus("ANSWERED");

            inquiryRepository.save(inquiry);

            Map<String, String> response = new HashMap<>();
            response.put("message", "답변이 등록되었습니다.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("권한")) {
                return ResponseEntity.status(403).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("답변 등록 중 오류가 발생했습니다.");
        }
    }

    // 문의 삭제
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteInquiry(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            validateAdminRole(httpRequest);
            if (!inquiryRepository.existsById(id)) {
                throw new RuntimeException("문의를 찾을 수 없습니다.");
            }

            inquiryRepository.deleteById(id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "문의가 삭제되었습니다.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("권한")) {
                return ResponseEntity.status(403).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("문의 삭제 중 오류가 발생했습니다.");
        }
    }

    // 문의 상태 변경
    @PatchMapping("/{id}/status")
    @Transactional
    public ResponseEntity<?> updateInquiryStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest request,
            HttpServletRequest httpRequest) {
        try {
            validateAdminRole(httpRequest);
            Inquiry inquiry = inquiryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

            if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("상태를 입력해주세요.");
            }

            String newStatus = request.getStatus().trim().toUpperCase();
            if (!newStatus.equals("PENDING") && !newStatus.equals("ANSWERED")) {
                return ResponseEntity.badRequest().body("유효하지 않은 상태입니다.");
            }

            inquiry.setStatus(newStatus);
            inquiryRepository.save(inquiry);

            Map<String, String> response = new HashMap<>();
            response.put("message", "문의 상태가 변경되었습니다.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("권한")) {
                return ResponseEntity.status(403).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("문의 상태 변경 중 오류가 발생했습니다.");
        }
    }

    // DTO 클래스들
    public static class AnswerRequest {
        private String answer;

        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
    }

    public static class StatusRequest {
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
