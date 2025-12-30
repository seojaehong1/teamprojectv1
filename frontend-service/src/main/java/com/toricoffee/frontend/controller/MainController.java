package com.toricoffee.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    // about, admin, bbs, mypage, owner, policy 등은 일단 그대로 유지
    // (나중에 필요하면 분리)
}