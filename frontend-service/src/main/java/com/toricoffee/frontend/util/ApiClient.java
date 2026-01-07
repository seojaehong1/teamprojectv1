package com.toricoffee.frontend.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ApiClient {

    @Autowired
    private RestTemplate restTemplate;

    private final String GATEWAY_URL = "http://localhost:8000";

    public <T> T get(String path, Class<T> responseType) {
        String url = GATEWAY_URL + path;
        return restTemplate.getForObject(url, responseType);
    }

    public <T> T post(String path, Object request, Class<T> responseType) {
        String url = GATEWAY_URL + path;
        return restTemplate.postForObject(url, request, responseType);
    }

    public void put(String path, Object request) {
        String url = GATEWAY_URL + path;
        restTemplate.put(url, request);
    }

    // 삭제를 위한 DELETE 메서드 추가
    public void delete(String path) {
        String url = GATEWAY_URL + path;
        restTemplate.delete(url);
    }
}