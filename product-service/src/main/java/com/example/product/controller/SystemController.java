package com.example.product.controller;

import com.example.product.dto.system.SystemInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    @Value("${spring.application.name:product-service}")
    private String serviceName;

    @Value("${app.version:1.0.0}")
    private String version;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Value("${server.port:8001}")
    private int serverPort;

    @GetMapping("/info")
    public Map<String, Object> getSystemInfo() {
        SystemInfoDto data = SystemInfoDto.builder()
                .serviceName(serviceName)
                .version(version)
                .javaVersion(System.getProperty("java.version"))
                .activeProfile(activeProfile)
                .serverPort(serverPort)
                .build();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "서버 정보 조회 성공");
        response.put("data", data);

        return response;
    }
}