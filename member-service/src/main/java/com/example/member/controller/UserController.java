package com.example.member.controller;

import com.example.member.dto.request.UpdateProfileRequest;
import com.example.member.dto.response.ApiResponse;
import com.example.member.dto.response.ProfileResponse;
import com.example.member.model.Member;
import com.example.member.service.MemberService;
import com.example.member.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    public UserController(MemberService memberService, JwtUtil jwtUtil) {
        this.memberService = memberService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<?>> getProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("로그인이 필요합니다.", "UNAUTHORIZED"));
            }

            String token = authHeader.substring(7);

            if (!jwtUtil.validateAccessToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("로그인이 필요합니다.", "UNAUTHORIZED"));
            }

            String userId = jwtUtil.getUserIdFromToken(token);
            Member member = memberService.getMemberByUserId(userId);

            ProfileResponse data = ProfileResponse.builder()
                    .userId(member.getUserId())
                    .name(member.getName())
                    .email(member.getEmail())
                    .userType(member.getUserType())
                    .createdAt(member.getCreatedAt() != null
                            ? member.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME)
                            : null)
                    .build();

            return ResponseEntity.ok(ApiResponse.success("회원정보 조회 성공", data));

        } catch (RuntimeException e) {
            if ("USER_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("사용자를 찾을 수 없습니다.", "USER_NOT_FOUND"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("서버 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR"));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<?>> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody UpdateProfileRequest request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("로그인이 필요합니다.", "UNAUTHORIZED"));
            }

            String token = authHeader.substring(7);

            if (!jwtUtil.validateAccessToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("로그인이 필요합니다.", "UNAUTHORIZED"));
            }

            String userId = jwtUtil.getUserIdFromToken(token);

            if (request.getName() == null || request.getName().length() < 2 || request.getName().length() > 20) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("이름은 2-20자로 입력해주세요.", "BAD_REQUEST"));
            }

            if (request.getEmail() == null || !request.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("올바른 이메일 형식이 아닙니다.", "BAD_REQUEST"));
            }

            Member member = memberService.updateProfile(userId, request.getName(), request.getEmail());

            ProfileResponse data = ProfileResponse.builder()
                    .userId(member.getUserId())
                    .name(member.getName())
                    .email(member.getEmail())
                    .userType(member.getUserType())
                    .createdAt(member.getCreatedAt() != null
                            ? member.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME)
                            : null)
                    .build();

            return ResponseEntity.ok(ApiResponse.success("회원정보가 수정되었습니다.", data));

        } catch (RuntimeException e) {
            String errorCode = e.getMessage();
            if ("DUPLICATE_EMAIL".equals(errorCode)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("이미 사용 중인 이메일입니다.", errorCode));
            }
            if ("USER_NOT_FOUND".equals(errorCode)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("사용자를 찾을 수 없습니다.", errorCode));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("서버 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR"));
        }
    }
}
