package com.toricoffee.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    // About 페이지
    @GetMapping("/about/brand")
    public String brand() {
        return "about/brand";
    }

    @GetMapping("/about/bi")
    public String bi() {
        return "about/bi";
    }

    @GetMapping("/about/map")
    public String map() {
        return "about/map";
    }

    // BBS 페이지
    @GetMapping("/bbs/notice")
    public String notice() {
        return "bbs/notice";
    }

    @GetMapping("/bbs/notice_detail")
    public String noticeDetail() {
        return "bbs/notice_detail";
    }

    @GetMapping("/bbs/faq")
    public String faq() {
        return "bbs/faq";
    }

    @GetMapping("/bbs/faq_detail")
    public String faqDetail() {
        return "bbs/faq_detail";
    }

    @GetMapping("/bbs/event")
    public String event() {
        return "bbs/event";
    }

    @GetMapping("/bbs/event_detail")
    public String eventDetail() {
        return "bbs/event_detail";
    }

    // Mypage 페이지
    @GetMapping("/mypage/index")
    public String mypage() {
        return "mypage/index";
    }

    @GetMapping("/mypage/edit")
    public String mypageEdit() {
        return "mypage/edit";
    }

    @GetMapping("/mypage/inquiry")
    public String mypageInquiry() {
        return "mypage/inquiry";
    }

    @GetMapping("/mypage/inquiry_detail")
    public String mypageInquiryDetail() {
        return "mypage/inquiry_detail";
    }

    @GetMapping("/mypage/inquiry_list")
    public String mypageInquiryList() {
        return "mypage/inquiry_list";
    }

    // Admin 페이지
    @GetMapping("/admin/user")
    public String adminUser() {
        return "admin/user";
    }

    @GetMapping("/admin/product")
    public String adminProduct() {
        return "admin/product";
    }

    @GetMapping("/admin/notice")
    public String adminNotice() {
        return "admin/notice";
    }

    @GetMapping("/admin/inquiry")
    public String adminInquiry() {
        return "admin/inquiry";
    }

    // Policy 페이지
    @GetMapping("/policy/privacy_policy")
    public String privacyPolicy() {
        return "policy/privacy_policy";
    }

    @GetMapping("/policy/service_policy")
    public String servicePolicy() {
        return "policy/service_policy";
    }
}