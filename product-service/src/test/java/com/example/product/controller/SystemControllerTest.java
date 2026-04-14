package com.example.product.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SystemController 단위 테스트
 */
@WebMvcTest(SystemController.class)
class SystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/system/info - 서버 정보 조회 성공")
    void getSystemInfo_Success() throws Exception {
        mockMvc.perform(get("/api/system/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("서버 정보 조회 성공")))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.serviceName", is("product-service")))
                .andExpect(jsonPath("$.data.version", is("1.0.0")))
                .andExpect(jsonPath("$.data.javaVersion").isNotEmpty())
                .andExpect(jsonPath("$.data.springBootVersion").isNotEmpty())
                .andExpect(jsonPath("$.data.buildTime").isNotEmpty())
                .andExpect(jsonPath("$.data.activeProfile").isNotEmpty())
                .andExpect(jsonPath("$.data.serverPort", is(8001)));
    }

    @Test
    @DisplayName("GET /api/system/info - Java 버전 형식 확인")
    void getSystemInfo_JavaVersionFormat() throws Exception {
        mockMvc.perform(get("/api/system/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.javaVersion", matchesPattern("\\d+.*")));
    }

    @Test
    @DisplayName("GET /api/system/info - 빌드 시간 형식 확인")
    void getSystemInfo_BuildTimeFormat() throws Exception {
        mockMvc.perform(get("/api/system/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.buildTime",
                        matchesPattern("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")));
    }
}