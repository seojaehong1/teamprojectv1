package com.toricoffee.frontend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * API 프록시 컨트롤러
 * Frontend(8005)에서 각 마이크로서비스로 API 요청을 프록시합니다.
 */
@RestController
@RequestMapping("/api")
public class ApiProxyController {

    private final RestTemplate restTemplate;

    // 각 서비스 URL
    private static final String MEMBER_SERVICE_URL = "http://localhost:8004";
    private static final String BOARD_SERVICE_URL = "http://localhost:8006";
    private static final String ADMIN_SERVICE_URL = "http://localhost:8007";
    private static final String PRODUCT_SERVICE_URL = "http://localhost:8002";
    private static final String ORDER_SERVICE_URL = "http://localhost:8003";
    private static final String INVENTORY_SERVICE_URL = "http://localhost:8008";

    public ApiProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ==================== Member Service 프록시 ====================

    @RequestMapping(value = "/auth/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> proxyAuth(HttpServletRequest request, @RequestBody(required = false) Map<String, Object> body) {
        return proxyRequest(request, body, MEMBER_SERVICE_URL);
    }

    @RequestMapping(value = "/users/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> proxyUsers(HttpServletRequest request, @RequestBody(required = false) Map<String, Object> body) {
        return proxyRequest(request, body, MEMBER_SERVICE_URL);
    }

    // ==================== Board Service 프록시 ====================

    @RequestMapping(value = "/notices/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> proxyNotices(HttpServletRequest request, @RequestBody(required = false) Map<String, Object> body) {
        return proxyRequest(request, body, BOARD_SERVICE_URL);
    }

    @RequestMapping(value = "/boards/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> proxyBoards(HttpServletRequest request, @RequestBody(required = false) Map<String, Object> body) {
        return proxyRequest(request, body, BOARD_SERVICE_URL);
    }

    @RequestMapping(value = "/comments/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> proxyComments(HttpServletRequest request, @RequestBody(required = false) Map<String, Object> body) {
        return proxyRequest(request, body, BOARD_SERVICE_URL);
    }

    // ==================== Admin Service 프록시 ====================

    // 회원 관리 API는 member-service로 프록시 (AdminController가 member-service에 있음)
    @RequestMapping(value = "/admin/users", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> proxyAdminUsersRoot(HttpServletRequest request, @RequestBody(required = false) Map<String, Object> body) {
        System.out.println("[Proxy] ========================================");
        System.out.println("[Proxy] Admin Users Root - routing to MEMBER_SERVICE: " + MEMBER_SERVICE_URL);
        System.out.println("[Proxy] Method: " + request.getMethod());
        System.out.println("[Proxy] URI: " + request.getRequestURI());
        System.out.println("[Proxy] ========================================");
        return proxyRequest(request, body, MEMBER_SERVICE_URL);
    }

    @RequestMapping(value = "/admin/users/{id}", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> proxyAdminUsersById(HttpServletRequest request, @RequestBody(required = false) Map<String, Object> body, @PathVariable String id) {
        System.out.println("[Proxy] ========================================");
        System.out.println("[Proxy] Admin Users By ID: " + id + " - routing to MEMBER_SERVICE: " + MEMBER_SERVICE_URL);
        System.out.println("[Proxy] Method: " + request.getMethod());
        System.out.println("[Proxy] URI: " + request.getRequestURI());
        System.out.println("[Proxy] ========================================");
        return proxyRequest(request, body, MEMBER_SERVICE_URL);
    }

    @RequestMapping(value = "/admin/users/{id}/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> proxyAdminUsersSub(HttpServletRequest request, @RequestBody(required = false) Map<String, Object> body, @PathVariable String id) {
        System.out.println("[Proxy] ========================================");
        System.out.println("[Proxy] Admin Users Sub path for ID: " + id + " - routing to MEMBER_SERVICE: " + MEMBER_SERVICE_URL);
        System.out.println("[Proxy] Method: " + request.getMethod());
        System.out.println("[Proxy] URI: " + request.getRequestURI());
        System.out.println("[Proxy] ========================================");
        return proxyRequest(request, body, MEMBER_SERVICE_URL);
    }

    // 공지사항 관리 API는 admin-service로 프록시
    @RequestMapping(value = "/admin/notices/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> proxyAdminNotices(HttpServletRequest request, @RequestBody(required = false) Map<String, Object> body) {
        return proxyRequest(request, body, ADMIN_SERVICE_URL);
    }

    @RequestMapping(value = "/admin/notices", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> proxyAdminNoticesRoot(HttpServletRequest request, @RequestBody(required = false) Map<String, Object> body) {
        return proxyRequest(request, body, ADMIN_SERVICE_URL);
    }

    // 문의 관리 API는 admin-service로 프록시
    @RequestMapping(value = "/admin/inquiries/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> proxyAdminInquiries(HttpServletRequest request, @RequestBody(required = false) Map<String, Object> body) {
        return proxyRequest(request, body, ADMIN_SERVICE_URL);
    }

    @RequestMapping(value = "/admin/inquiries", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> proxyAdminInquiriesRoot(HttpServletRequest request, @RequestBody(required = false) Map<String, Object> body) {
        return proxyRequest(request, body, ADMIN_SERVICE_URL);
    }

    @RequestMapping(value = "/inquiries/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> proxyInquiries(HttpServletRequest request, @RequestBody(required = false) Map<String, Object> body) {
        return proxyRequest(request, body, ADMIN_SERVICE_URL);
    }

    @RequestMapping(value = "/inquiries", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> proxyInquiriesRoot(HttpServletRequest request, @RequestBody(required = false) Map<String, Object> body) {
        return proxyRequest(request, body, ADMIN_SERVICE_URL);
    }

    // ==================== Product Service 프록시 ====================

    @RequestMapping(value = "/products/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> proxyProducts(HttpServletRequest request, @RequestBody(required = false) Map<String, Object> body) {
        return proxyRequest(request, body, PRODUCT_SERVICE_URL);
    }

    // ==================== Order Service 프록시 ====================

    @RequestMapping(value = "/orders/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> proxyOrders(HttpServletRequest request, @RequestBody(required = false) Map<String, Object> body) {
        return proxyRequest(request, body, ORDER_SERVICE_URL);
    }

    // ==================== Inventory Service 프록시 ====================

    @RequestMapping(value = "/inventory/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> proxyInventory(HttpServletRequest request, @RequestBody(required = false) Map<String, Object> body) {
        return proxyRequest(request, body, INVENTORY_SERVICE_URL);
    }

    // ==================== 공통 프록시 메서드 ====================

    private ResponseEntity<?> proxyRequest(HttpServletRequest request, Map<String, Object> body, String targetServiceUrl) {
        try {
            // 원본 URI와 쿼리 스트링 가져오기
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();
            String targetUrl = targetServiceUrl + uri + (queryString != null ? "?" + queryString : "");

            System.out.println("[Proxy] Request: " + request.getMethod() + " " + targetUrl);
            if (body != null) {
                System.out.println("[Proxy] Body: " + body);
            }

            // 헤더 복사
            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                // Host 헤더는 제외 (대상 서비스에 맞게 변경됨)
                if (!"host".equalsIgnoreCase(headerName) && !"content-length".equalsIgnoreCase(headerName)) {
                    headers.set(headerName, request.getHeader(headerName));
                }
            }
            headers.setContentType(MediaType.APPLICATION_JSON);

            // HTTP 메서드 결정
            HttpMethod method = HttpMethod.valueOf(request.getMethod());

            // 요청 생성
            HttpEntity<?> entity;
            if (body != null && (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH)) {
                entity = new HttpEntity<>(body, headers);
            } else {
                entity = new HttpEntity<>(headers);
            }

            // 요청 전송
            ResponseEntity<Object> response = restTemplate.exchange(targetUrl, method, entity, Object.class);
            System.out.println("[Proxy] Response Status: " + response.getStatusCode());
            System.out.println("[Proxy] Response Body: " + response.getBody());
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

        } catch (HttpClientErrorException e) {
            // 4xx 에러 - 클라이언트 에러
            try {
                return ResponseEntity.status(e.getStatusCode())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e.getResponseBodyAsString());
            } catch (Exception ex) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "요청 처리 실패");
                errorResponse.put("errorCode", e.getStatusCode().toString());
                return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
            }
        } catch (HttpServerErrorException e) {
            // 5xx 에러 - 서버 에러
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "서버 오류가 발생했습니다.");
            errorResponse.put("errorCode", "SERVER_ERROR");
            return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
        } catch (ResourceAccessException e) {
            // 연결 실패 - 대상 서비스가 실행 중이지 않음
            System.err.println("[Proxy Error] 서비스 연결 실패: " + targetServiceUrl + " - " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "서비스에 연결할 수 없습니다. 서비스가 실행 중인지 확인해주세요. (" + targetServiceUrl + ")");
            errorResponse.put("errorCode", "SERVICE_UNAVAILABLE");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "요청 처리 중 오류가 발생했습니다: " + e.getMessage());
            errorResponse.put("errorCode", "INTERNAL_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

