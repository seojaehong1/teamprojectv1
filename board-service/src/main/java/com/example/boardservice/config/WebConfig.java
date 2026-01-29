package com.example.boardservice.config;

import com.example.boardservice.config.JwtAuthenticationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final com.example.boardservice.config.JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    public WebConfig(JwtAuthenticationInterceptor jwtAuthenticationInterceptor) {
        this.jwtAuthenticationInterceptor = jwtAuthenticationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                // JWT 검증이 필요한 경로
                .addPathPatterns("/api/boards/**", "/api/comments/**", "/api/notices/**")
                // JWT 검증 제외 경로는 인터셉터 내에서 HTTP 메서드로 구분
                .excludePathPatterns();
    }
}
