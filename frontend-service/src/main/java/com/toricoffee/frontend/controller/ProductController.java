package com.toricoffee.frontend.controller;

import com.toricoffee.frontend.util.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/menu")
public class ProductController {

    @Autowired
    private ApiClient apiClient;

    // 메뉴 목록 페이지 GET /menu/drink
    @GetMapping("/drink")
    public String drinkList() {
        return "menu/drink";
    }

    // 메뉴 디테일 페이지
    @GetMapping("/drink-detail")
    public String drinkDetail(
            @RequestParam Long menuCode
    ) {
        log.info("음료 상세 페이지 진입 menuCode={}", menuCode);
        return "menu/drink_detail";
    }
}