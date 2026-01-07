package com.du.adminservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notices")
public class AdminNoticeController {

    private final RestTemplate restTemplate;
    private static final String BOARD_SERVICE_URL = "http://localhost:8006";

    public AdminNoticeController() {
        this.restTemplate = new RestTemplate();
    }

    // 공통 헤더 생성 헬퍼 메서드
    private HttpHeaders createAdminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Role", "ADMIN");
        return headers;
    }

    // isPinned → isImportant 변환 헬퍼 메서드
    private Map<String, Object> convertNoticeData(Map<String, Object> request) {
        Map<String, Object> noticeData = new java.util.HashMap<>(request);
        if (noticeData.containsKey("isPinned")) {
            noticeData.put("isImportant", noticeData.get("isPinned"));
        }
        return noticeData;
    }

    // 공지사항 목록 조회 (board-service에서 가져옴)
    @GetMapping
    public ResponseEntity<?> getNoticeList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "title") String searchType) {
        try {
            String url = BOARD_SERVICE_URL + "/api/notices?page=" + page + "&limit=" + size;
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 먼저 URL 디코딩 (프론트엔드에서 이미 인코딩되어 올 수 있음)
                String decodedKeyword = URLDecoder.decode(keyword.trim(), StandardCharsets.UTF_8);
                // 다시 인코딩해서 board-service로 전달
                String encodedKeyword = URLEncoder.encode(decodedKeyword, StandardCharsets.UTF_8);
                url += "&keyword=" + encodedKeyword + "&searchType=" + searchType;
                System.out.println("[AdminNoticeController] keyword (원본): " + keyword + ", (디코딩): " + decodedKeyword);
            }

            System.out.println("[AdminNoticeController] Calling board-service: " + url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("공지사항 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 공지사항 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> getNoticeDetail(@PathVariable Long id) {
        try {
            String url = BOARD_SERVICE_URL + "/api/notices/" + id;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(500).body("공지사항 조회 중 오류가 발생했습니다.");
        }
    }

    // 공지사항 등록 (board-service로 전달)
    @PostMapping
    public ResponseEntity<?> createNotice(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        try {
            String url = BOARD_SERVICE_URL + "/api/notices/admin";
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(convertNoticeData(request), createAdminHeaders());
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("공지사항 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 공지사항 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNotice(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            String url = BOARD_SERVICE_URL + "/api/notices/admin/" + id;
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(convertNoticeData(request), createAdminHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("공지사항 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 공지사항 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotice(@PathVariable Long id) {
        try {
            String url = BOARD_SERVICE_URL + "/api/notices/admin/" + id;
            HttpEntity<?> entity = new HttpEntity<>(createAdminHeaders());
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            return ResponseEntity.ok(Map.of("message", "공지사항이 삭제되었습니다."));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("공지사항 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 공지 고정 토글
    @PatchMapping("/{id}/pin")
    public ResponseEntity<?> togglePin(@PathVariable Long id) {
        try {
            String url = BOARD_SERVICE_URL + "/api/notices/admin/" + id + "/toggle-pin";
            HttpEntity<?> entity = new HttpEntity<>(createAdminHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, Map.class);
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("공지 고정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
