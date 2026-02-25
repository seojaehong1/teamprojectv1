package com.du.adminservice.controller;

import com.du.adminservice.model.Inquiry;
import com.du.adminservice.repository.InquiryRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 일반 회원용 문의 API
 * - 문의 작성
 * - 내 문의 목록 조회
 * - 내 문의 상세 조회
 */
@RestController
@RequestMapping("/api/inquiries")
public class InquiryController {

    private final InquiryRepository inquiryRepository;

    public InquiryController(InquiryRepository inquiryRepository) {
        this.inquiryRepository = inquiryRepository;
    }

    // 문의 작성
    @PostMapping
    public ResponseEntity<?> createInquiry(
            @RequestBody CreateInquiryRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userId = (String) httpRequest.getAttribute("userId");

            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }

            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("제목을 입력해주세요.");
            }

            if (request.getTitle().length() > 200) {
                return ResponseEntity.badRequest().body("제목은 200자 이내로 입력해주세요.");
            }

            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("내용을 입력해주세요.");
            }

            if (request.getContent().length() > 5000) {
                return ResponseEntity.badRequest().body("내용은 5000자 이내로 입력해주세요.");
            }

            Inquiry inquiry = new Inquiry();
            inquiry.setTitle(request.getTitle().trim());
            inquiry.setContent(request.getContent().trim());
            inquiry.setUserId(userId);
            inquiry.setStatus("PENDING");

            Inquiry savedInquiry = inquiryRepository.save(inquiry);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "문의가 등록되었습니다.");
            response.put("inquiryId", savedInquiry.getInquiryId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("문의 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 내 문의 목록 조회
    @GetMapping("/my")
    public ResponseEntity<?> getMyInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        try {
            String userId = (String) httpRequest.getAttribute("userId");

            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }

            // 페이지 크기 제한 (최대 50)
            if (size > 50) size = 50;
            if (size < 1) size = 10;

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Inquiry> inquiryPage = inquiryRepository.findByUserId(userId, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("inquiries", inquiryPage.getContent());
            response.put("currentPage", inquiryPage.getNumber());
            response.put("totalPages", inquiryPage.getTotalPages());
            response.put("totalItems", inquiryPage.getTotalElements());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("문의 목록 조회 중 오류가 발생했습니다.");
        }
    }

    // 내 문의 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> getInquiryDetail(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            String userId = (String) httpRequest.getAttribute("userId");

            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }

            Inquiry inquiry = inquiryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

            // 본인 문의만 조회 가능
            if (!inquiry.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body("본인의 문의만 조회할 수 있습니다.");
            }

            return ResponseEntity.ok(inquiry);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("문의 조회 중 오류가 발생했습니다.");
        }
    }

    // DTO 클래스
    public static class CreateInquiryRequest {
        private String title;
        private String content;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
