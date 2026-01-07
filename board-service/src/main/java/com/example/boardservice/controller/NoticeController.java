package com.example.boardservice.controller;

import com.example.boardservice.model.Notice;
import com.example.boardservice.service.NoticeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {
    
    private final NoticeService noticeService;
    
    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }
    
    // =============================================
    // 일반 사용자용 API (읽기 전용)
    // =============================================
    
    // 전체 공지사항 조회 (페이징, 누구나 가능)
    @GetMapping
    public ResponseEntity<?> getAllNotices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "title") String searchType) {

        // page는 1부터 시작, 내부적으로 0부터 시작하는 인덱스로 변환
        int pageIndex = Math.max(0, page - 1);

        // 페이징 처리 (고정 공지는 항상 최상단, 나머지는 최신순)
        Pageable pageable = PageRequest.of(pageIndex, limit, Sort.by("createdAt").descending());
        Page<Notice> noticePage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            // URL 디코딩 처리 (한글 검색어 지원)
            String decodedKeyword = URLDecoder.decode(keyword.trim(), StandardCharsets.UTF_8);
            System.out.println("[NoticeController] 검색 요청 - keyword: " + decodedKeyword + ", searchType: " + searchType);
            
            // 검색 타입에 따라 다른 검색 수행
            switch (searchType.toLowerCase()) {
                case "content":
                    noticePage = noticeService.searchNoticesByContent(decodedKeyword, pageable);
                    break;
                case "author":
                    noticePage = noticeService.searchNoticesByAuthor(decodedKeyword, pageable);
                    break;
                case "title":
                default:
                    noticePage = noticeService.searchNoticesByTitle(decodedKeyword, pageable);
                    break;
            }
            System.out.println("[NoticeController] 검색 결과 - 총 " + noticePage.getTotalElements() + "건");
        } else {
            noticePage = noticeService.getAllNoticesWithPaging(pageable);
            System.out.println("[NoticeController] 전체 목록 조회 - 총 " + noticePage.getTotalElements() + "건");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("notices", noticePage.getContent());
        response.put("currentPage", page);
        response.put("totalPages", noticePage.getTotalPages());
        response.put("totalItems", noticePage.getTotalElements());

        System.out.println("[NoticeController] 응답 데이터 - notices 개수: " + noticePage.getContent().size());
        return ResponseEntity.ok(response);
    }
    
    // 공지사항 상세 조회 (누구나 가능)
    @GetMapping("/{id}")
    public ResponseEntity<?> getNoticeById(@PathVariable Long id) {
        try {
            Notice notice = noticeService.getNoticeById(id);
            return ResponseEntity.ok(notice);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // =============================================
    // 관리자 전용 API
    // =============================================
    
    // 공지사항 작성 (관리자 전용)
    @PostMapping("/admin")
    public ResponseEntity<?> createNotice(
            @RequestBody CreateNoticeRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userId = (String) httpRequest.getAttribute("userId");
            String role = (String) httpRequest.getAttribute("role");

            // X-User-Role 헤더에서도 확인 (admin-service에서 호출 시)
            if (role == null) {
                role = httpRequest.getHeader("X-User-Role");
            }
            if (userId == null) {
                userId = "관리자"; // admin-service에서 호출 시 기본값
            }

            if (!"ADMIN".equalsIgnoreCase(role)) {
                return ResponseEntity.status(403).body("관리자만 공지사항을 작성할 수 있습니다.");
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

            if (request.getContent().length() > 10000) {
                return ResponseEntity.badRequest().body("내용은 10000자 이내로 입력해주세요.");
            }
            
            Notice notice = new Notice();
            notice.setTitle(request.getTitle().trim());
            notice.setContent(request.getContent().trim());
            notice.setAuthor("관리자"); // 항상 "관리자"로 설정
            notice.setIsPinned(request.getIsImportant() != null ? request.getIsImportant() : false);

            Notice savedNotice = noticeService.createNotice(notice, role);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "공지사항이 등록되었습니다.");
            response.put("noticeId", savedNotice.getNoticeId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // 공지사항 수정 (관리자 전용)
    @PutMapping("/admin/{id}")
    public ResponseEntity<?> updateNotice(
            @PathVariable Long id,
            @RequestBody UpdateNoticeRequest request,
            HttpServletRequest httpRequest) {
        try {
            String role = (String) httpRequest.getAttribute("role");
            if (role == null) {
                role = httpRequest.getHeader("X-User-Role");
            }

            if (!"ADMIN".equalsIgnoreCase(role)) {
                return ResponseEntity.status(403).body("관리자만 공지사항을 수정할 수 있습니다.");
            }
            
            Notice updatedNotice = new Notice();
            updatedNotice.setTitle(request.getTitle());
            updatedNotice.setContent(request.getContent());
            updatedNotice.setIsPinned(request.getIsImportant());
            
            noticeService.updateNotice(id, updatedNotice, role);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "공지사항이 수정되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // 공지사항 삭제 (관리자 전용)
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteNotice(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            String role = (String) httpRequest.getAttribute("role");
            if (role == null) {
                role = httpRequest.getHeader("X-User-Role");
            }

            if (!"ADMIN".equalsIgnoreCase(role)) {
                return ResponseEntity.status(403).body("관리자만 공지사항을 삭제할 수 있습니다.");
            }
            
            noticeService.deleteNotice(id, role);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "공지사항이 삭제되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // 공지 고정 토글 (관리자 전용)
    @PatchMapping("/admin/{id}/toggle-pin")
    public ResponseEntity<?> togglePinned(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            String role = (String) httpRequest.getAttribute("role");
            if (role == null) {
                role = httpRequest.getHeader("X-User-Role");
            }

            if (!"ADMIN".equalsIgnoreCase(role)) {
                return ResponseEntity.status(403).body("관리자만 공지 고정을 설정할 수 있습니다.");
            }

            Notice notice = noticeService.togglePinned(id, role);

            Map<String, Object> response = new HashMap<>();
            response.put("message", notice.getIsPinned() ? "공지가 고정되었습니다." : "공지 고정이 해제되었습니다.");
            response.put("isPinned", notice.getIsPinned());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // DTO 클래스들
    public static class CreateNoticeRequest {
        private String title;
        private String content;
        private Boolean isImportant;
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Boolean getIsImportant() { return isImportant; }
        public void setIsImportant(Boolean isImportant) { this.isImportant = isImportant; }
    }
    
    public static class UpdateNoticeRequest {
        private String title;
        private String content;
        private Boolean isImportant;
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Boolean getIsImportant() { return isImportant; }
        public void setIsImportant(Boolean isImportant) { this.isImportant = isImportant; }
    }
}
