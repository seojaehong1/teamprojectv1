package com.du.adminservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthenticationInterceptor jwtAuthenticationInterceptor;
    private final UserJwtAuthenticationInterceptor userJwtAuthenticationInterceptor;

    public WebConfig(JwtAuthenticationInterceptor jwtAuthenticationInterceptor,
                     UserJwtAuthenticationInterceptor userJwtAuthenticationInterceptor) {
        this.jwtAuthenticationInterceptor = jwtAuthenticationInterceptor;
        this.userJwtAuthenticationInterceptor = userJwtAuthenticationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 관리자 전용 API
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/login");

        // 일반 사용자용 API (로그인만 필요, 관리자 권한 불필요)
        registry.addInterceptor(userJwtAuthenticationInterceptor)
                .addPathPatterns("/api/inquiries/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
