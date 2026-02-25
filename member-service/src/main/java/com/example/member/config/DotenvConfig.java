package com.example.member.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class DotenvConfig {

    @PostConstruct
    public void loadEnv() {
        // .env 파일은 선택사항 - 없어도 application.properties에서 설정 읽음
        try {
            Dotenv dotenv = null;
            String[] possiblePaths = {"./", "../", "../../", "C:/_dev5/teamprj/teamprojectv1/"};

            for (String path : possiblePaths) {
                File envFile = new File(path + ".env");
                if (envFile.exists()) {
                    try {
                        dotenv = Dotenv.configure()
                                .directory(path)
                                .ignoreIfMissing()
                                .load();
                        System.out.println(".env found at: " + envFile.getAbsolutePath());
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
                System.out.println(".env file loaded!");
                System.out.println("MAIL_USERNAME: " + (System.getProperty("MAIL_USERNAME") != null ? "configured" : "missing"));
            } else {
                System.out.println(".env file not found - using application.properties");
            }
        } catch (Exception e) {
            // .env 없어도 정상 작동 - application.properties 사용
            System.out.println(".env file error - using application.properties: " + e.getMessage());
        }
    }
}
