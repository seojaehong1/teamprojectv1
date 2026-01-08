package com.toricoffee.frontend.controller;

import com.toricoffee.frontend.util.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ApiClient apiClient;

    @GetMapping("/products")
    public String productManagePage() {
        log.info("관리자 상품 관리 페이지 진입");
        return "admin/product"; // templates/admin/product.html
    }

    @GetMapping("/users")
    public String userManagePage() {
        log.info("관리자 회원 관리 페이지 진입");
        return "admin/user";
    }

    @GetMapping("/notices")
    public String noticeManagePage() {
        log.info("관리자 공지사항 관리 페이지 진입");
        return "admin/notice";
    }

    @GetMapping("/inquiries")
    public String inquiryManagePage() {
        log.info("관리자 문의 관리 페이지 진입");
        return "admin/inquiry";
    }

}
