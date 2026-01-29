package com.example.boardservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // Local development
        config.addAllowedOrigin("http://localhost:8000");
        config.addAllowedOrigin("http://localhost:8004");
        config.addAllowedOrigin("http://localhost:8005");
        config.addAllowedOrigin("http://localhost:8006");
        config.addAllowedOrigin("http://localhost:8007");
        // Kubernetes internal services
        config.addAllowedOrigin("http://gateway-service:8000");
        config.addAllowedOrigin("http://frontend-service:8005");
        config.addAllowedOrigin("http://member-service:8004");
        config.addAllowedOrigin("http://board-service:8006");
        config.addAllowedOrigin("http://admin-service:8007");
        // Allow all origins for K8s ALB/Ingress
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}

