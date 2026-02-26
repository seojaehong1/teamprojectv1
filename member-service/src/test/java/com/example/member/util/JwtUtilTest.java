package com.example.member.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JwtUtil 단위 테스트
 * JWT 토큰 생성, 검증, 파싱 기능을 테스트합니다.
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String TEST_SECRET = "your-256-bit-secret-key-here-must-be-at-least-32-characters-long";
    private static final Long ACCESS_EXPIRATION = 3600000L; // 1시간
    private static final Long REFRESH_EXPIRATION = 604800000L; // 7일

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "accessExpiration", ACCESS_EXPIRATION);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", REFRESH_EXPIRATION);
    }

    @Test
    @DisplayName("Access Token 생성 성공")
    void generateAccessToken_Success() {
        // given
        String userId = "testUser";
        String role = "member";

        // when
        String token = jwtUtil.generateAccessToken(userId, role);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT는 header.payload.signature 형태
    }

    @Test
    @DisplayName("Refresh Token 생성 성공")
    void generateRefreshToken_Success() {
        // given
        String userId = "testUser";

        // when
        String token = jwtUtil.generateRefreshToken(userId);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("토큰에서 사용자 ID 추출 성공")
    void getUserIdFromToken_Success() {
        // given
        String userId = "testUser";
        String token = jwtUtil.generateAccessToken(userId, "member");

        // when
        String extractedUserId = jwtUtil.getUserIdFromToken(token);

        // then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("토큰에서 역할(role) 추출 성공")
    void getRoleFromToken_Success() {
        // given
        String userId = "testUser";
        String role = "admin";
        String token = jwtUtil.generateAccessToken(userId, role);

        // when
        String extractedRole = jwtUtil.getRoleFromToken(token);

        // then
        assertThat(extractedRole).isEqualTo(role);
    }

    @Test
    @DisplayName("유효한 Access Token 검증 성공")
    void validateAccessToken_ValidToken_ReturnsTrue() {
        // given
        String token = jwtUtil.generateAccessToken("testUser", "member");

        // when
        boolean isValid = jwtUtil.validateAccessToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("유효한 Refresh Token 검증 성공")
    void validateRefreshToken_ValidToken_ReturnsTrue() {
        // given
        String token = jwtUtil.generateRefreshToken("testUser");

        // when
        boolean isValid = jwtUtil.validateRefreshToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Access Token으로 Refresh Token 검증 실패")
    void validateRefreshToken_WithAccessToken_ReturnsFalse() {
        // given
        String accessToken = jwtUtil.generateAccessToken("testUser", "member");

        // when
        boolean isValid = jwtUtil.validateRefreshToken(accessToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Refresh Token으로 Access Token 검증 실패")
    void validateAccessToken_WithRefreshToken_ReturnsFalse() {
        // given
        String refreshToken = jwtUtil.generateRefreshToken("testUser");

        // when
        boolean isValid = jwtUtil.validateAccessToken(refreshToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("잘못된 토큰 검증 실패")
    void validateAccessToken_InvalidToken_ReturnsFalse() {
        // given
        String invalidToken = "invalid.token.here";

        // when
        boolean isValid = jwtUtil.validateAccessToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("블랙리스트에 추가된 토큰 검증 실패")
    void validateAccessToken_BlacklistedToken_ReturnsFalse() {
        // given
        String token = jwtUtil.generateAccessToken("testUser", "member");
        jwtUtil.addToBlacklist(token);

        // when
        boolean isValid = jwtUtil.validateAccessToken(token);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("잘못된 토큰에서 사용자 ID 추출 시 예외 발생")
    void getUserIdFromToken_InvalidToken_ThrowsException() {
        // given
        String invalidToken = "invalid.token.here";

        // when & then
        assertThatThrownBy(() -> jwtUtil.getUserIdFromToken(invalidToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("토큰에서 사용자 ID를 추출할 수 없습니다");
    }

    @Test
    @DisplayName("여러 역할로 토큰 생성 및 검증")
    void generateAndValidateTokens_DifferentRoles() {
        // given
        String[] roles = {"member", "admin", "store_owner"};

        for (String role : roles) {
            // when
            String token = jwtUtil.generateAccessToken("user_" + role, role);
            String extractedRole = jwtUtil.getRoleFromToken(token);

            // then
            assertThat(extractedRole).isEqualTo(role);
            assertThat(jwtUtil.validateAccessToken(token)).isTrue();
        }
    }

    @Test
    @DisplayName("기존 호환성 메서드 테스트 - generateToken")
    void generateToken_BackwardCompatibility() {
        // given
        String userId = "testUser";
        String role = "member";

        // when
        String token = jwtUtil.generateToken(userId, role);

        // then
        assertThat(token).isNotNull();
        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtUtil.extractRole(token)).isEqualTo(role);
    }
}