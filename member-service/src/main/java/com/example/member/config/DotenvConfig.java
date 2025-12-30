package com.example.member.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotenvConfig {

    @PostConstruct
    public void loadEnv() {
        // 프로젝트 루트의 .env 파일 읽기
        Dotenv dotenv = Dotenv.configure()
                .directory("./")  // 현재 디렉토리 (member-service)
                .ignoreIfMissing()
                .load();

        // 상위 디렉토리의 .env 파일 시도
        if (dotenv.entries().isEmpty()) {
            dotenv = Dotenv.configure()
                    .directory("../")  // 상위 디렉토리 (msaV1-main9)
                    .ignoreIfMissing()
                    .load();
        }

        // 환경변수로 설정
        dotenv.entries().forEach(entry -> 
            System.setProperty(entry.getKey(), entry.getValue())
        );
    }
}
