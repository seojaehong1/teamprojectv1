package com.du.adminservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/notices")
public class AdminNoticeController {

    private final RestTemplate restTemplate;
    private static final String BOARD_SERVICE_URL = "http://localhost:8006";

    public AdminNoticeController() {
        this.restTemplate = new RestTemplate();
    }

    // 공지사항 목록 조회 (board-service에서 가져옴)
    @GetMapping
    public ResponseEntity<?> getNoticeList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        try {
            String url = BOARD_SERVICE_URL + "/api/notices?page=" + page + "&limit=" + size;
            if (keyword != null && !keyword.trim().isEmpty()) {
                url += "&keyword=" + keyword;
            }

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

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Role", "ADMIN");

            // isPinned를 board-service 형식에 맞게 변환 (isImportant로 변환)
            Map<String, Object> noticeData = new java.util.HashMap<>(request);
            // 프론트엔드에서 isPinned로 보내면 isImportant로 변환
            if (noticeData.containsKey("isPinned")) {
                noticeData.put("isImportant", noticeData.get("isPinned"));
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(noticeData, headers);

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

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Role", "ADMIN");

            // isPinned를 isImportant로 변환
            Map<String, Object> noticeData = new java.util.HashMap<>(request);
            if (noticeData.containsKey("isPinned")) {
                noticeData.put("isImportant", noticeData.get("isPinned"));
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(noticeData, headers);

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

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Role", "ADMIN");

            HttpEntity<?> entity = new HttpEntity<>(headers);

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

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Role", "ADMIN");

            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, Map.class);
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("공지 고정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
