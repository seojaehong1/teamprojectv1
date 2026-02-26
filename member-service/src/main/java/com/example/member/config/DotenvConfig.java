package com.example.member.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class DotenvConfig {

    private static final Logger log = LoggerFactory.getLogger(DotenvConfig.class);

    @PostConstruct
    public void loadEnv() {
        // .env 파일은 선택사항 - 없어도 application.properties에서 설정 읽음
        try {
            Dotenv dotenv = null;
            String[] possiblePaths = {"./", "../", "../../"};

            for (String path : possiblePaths) {
                File envFile = new File(path + ".env");
                if (envFile.exists()) {
                    try {
                        dotenv = Dotenv.configure()
                                .directory(path)
                                .ignoreIfMissing()
                                .load();
                        log.info(".env found at: {}", envFile.getAbsolutePath());
                        break;
                    } catch (Exception e) {
                        // 다음 경로 시도
                    }
                }
            }

            if (dotenv != null && !dotenv.entries().isEmpty()) {
                dotenv.entries().forEach(entry -> {
                    if (System.getProperty(entry.getKey()) == null) {
                        System.setProperty(entry.getKey(), entry.getValue());
                    }
                });
                log.info(".env file loaded!");
                log.debug("MAIL_USERNAME: {}", System.getProperty("MAIL_USERNAME") != null ? "configured" : "missing");
            } else {
                log.info(".env file not found - using application.properties");
            }
        } catch (Exception e) {
            // .env 없어도 정상 작동 - application.properties 사용
            log.warn(".env file error - using application.properties: {}", e.getMessage());
        }
    }
}
