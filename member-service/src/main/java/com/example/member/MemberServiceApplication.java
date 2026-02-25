package com.example.member;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MemberServiceApplication {
    public static void main(String[] args) {
        // .env 파일 로드 (프로젝트 루트 디렉토리에서 .env 파일 읽기)
        Dotenv dotenv = Dotenv.configure()
                .directory("../") // member-service의 상위 디렉토리 (teamprojectv1/)
                .ignoreIfMissing() // .env 파일이 없어도 에러 없이 진행
                .load();

        // 환경변수를 시스템 프로퍼티로 설정
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });

        SpringApplication.run(MemberServiceApplication.class, args);
    }
}
