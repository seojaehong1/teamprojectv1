package com.example.product.controller;

import com.example.product.dto.system.SystemInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final Environment environment;

    @Value("${server.port:8001}")
    private int serverPort;

    @Value("${spring.application.name:product-service}")
    private String serviceName;

    private String serverStartTime;

    @PostConstruct
    public void init() {
        this.serverStartTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @GetMapping("/info")
    public Map<String, Object> getSystemInfo() {
        String[] activeProfiles = environment.getActiveProfiles();
        String activeProfile = activeProfiles.length > 0
                ? String.join(", ", activeProfiles)
                : "default";

        SystemInfoDto systemInfo = SystemInfoDto.builder()
                .serviceName(serviceName)
                .version("1.0.0")
                .javaVersion(System.getProperty("java.version"))
                .springBootVersion(SpringBootVersion.getVersion())
                .buildTime(serverStartTime)
                .activeProfile(activeProfile)
                .serverPort(serverPort)
                .build();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "서버 정보 조회 성공");
        response.put("data", systemInfo);

        return response;
    }
}