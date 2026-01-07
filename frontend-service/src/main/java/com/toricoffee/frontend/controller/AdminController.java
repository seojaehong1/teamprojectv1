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
@RequestMapping("/admin/products")
public class AdminController {

    @Autowired
    private ApiClient apiClient;


    // 관리자 상품 관리 페이지
    // 실제 주소 GET /admin/products
    // 템플릿 templates/admin/product.html
    @GetMapping
    public String productPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            Model model
    ) {
        try {
            Object productPageData;

            // 🔹 검색 여부에 따라 API 분기
            if (keyword == null || keyword.isBlank()) {
                productPageData = apiClient.get(
                        "/api/admin/products?page=" + page + "&limit=" + limit,
                        Object.class
                );
            } else {
                productPageData = apiClient.get(
                        "/api/admin/products/search"
                                + "?keyword=" + keyword
                                + "&page=" + page
                                + "&limit=" + limit,
                        Object.class
                );
            }

            model.addAttribute("products", productPageData);
            model.addAttribute("keyword", keyword);
            model.addAttribute("currentPage", page);

            log.info("관리자 상품 목록 로드 성공");

        } catch (Exception e) {
            log.error("관리자 상품 목록 로드 실패", e);

            model.addAttribute("products", null);
            model.addAttribute("errorMessage", "상품 목록을 불러오지 못했습니다.");
        }

        return "admin/product";
    }

    @GetMapping("/form-data")
    public String productFormData(Model model) {

        try {
            Object allergies = apiClient.get("/api/admin/allergies", Object.class);
            Object materials = apiClient.get("/api/admin/materials", Object.class);
            Object options   = apiClient.get("/api/admin/options", Object.class);

            model.addAttribute("allergies", allergies);
            model.addAttribute("materials", materials);
            model.addAttribute("options", options);

            log.info("상품 등록/수정 selector 데이터 로드 성공");

        } catch (Exception e) {
            log.error("상품 등록 selector 데이터 로드 실패", e);
        }

        return "admin/product :: formData";
        // thymeleaf fragment 사용하지 않으면 "" 리턴
    }
}
