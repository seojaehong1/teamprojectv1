package com.example.boardservice.config;

import com.example.boardservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 요청은 통과
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        
        // GET 요청 중 조회만 가능한 경로는 JWT 검증 제외
        if ("GET".equalsIgnoreCase(method)) {
            // 게시글 목록 조회
            if ("/api/boards".equals(requestPath)) {
                return true;
            }
            // 게시글 상세 조회 (숫자 ID만)
            if (requestPath.matches("/api/boards/\\d+")) {
                return true;
            }
            // 작성자별 조회
            if (requestPath.startsWith("/api/boards/author/")) {
                return true;
            }
            // 특정 게시글의 댓글 조회
            if (requestPath.startsWith("/api/comments/board/")) {
                return true;
            }
            // 작성자별 댓글 조회
            if (requestPath.startsWith("/api/comments/author/")) {
                return true;
            }
            // 공지사항 목록/상세 조회 (누구나 가능)
            if ("/api/notices".equals(requestPath) || requestPath.matches("/api/notices/\\d+")) {
                return true;
            }
        }

        // admin-service에서 X-User-Role 헤더로 호출하는 경우 (서비스 간 통신)
        String userRoleHeader = request.getHeader("X-User-Role");
        if ("ADMIN".equalsIgnoreCase(userRoleHeader) && requestPath.startsWith("/api/notices/admin")) {
            request.setAttribute("role", "ADMIN");
            request.setAttribute("userId", "admin");
            return true;
        }

        // Authorization 헤더에서 토큰 추출
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"로그인이 필요합니다.\"}");
            return false;
        }

        String token = authHeader.substring(7); // "Bearer " 제거

        // 토큰 검증
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"유효하지 않은 토큰입니다.\"}");
            return false;
        }

        // userId와 role을 request attribute에 저장 (컨트롤러에서 사용)
        try {
            String userId = jwtUtil.getUserIdFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            
            if (userId == null || userId.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"토큰에서 사용자 ID를 추출할 수 없습니다.\"}");
                return false;
            }
            
            request.setAttribute("userId", userId);
            request.setAttribute("role", role);  // role도 저장!
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"토큰 처리 중 오류가 발생했습니다: " + e.getMessage() + "\"}");
            return false;
        }

        return true;
    }
}
