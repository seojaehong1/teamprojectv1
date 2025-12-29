package com.toricoffee.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    // 루트
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/find_id")
    public String findId() {
        return "find_id";
    }

    @GetMapping("/find_password")
    public String findPassword() {
        return "find_password";
    }

    // about
    @GetMapping("/about/brand")
    public String aboutBrand() {
        return "about/brand";
    }

    @GetMapping("/about/bi")
    public String aboutBi() {
        return "about/bi";
    }

    @GetMapping("/about/map")
    public String aboutMap() {
        return "about/map";
    }

    // admin
    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/login";
    }

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

    // bbs
    @GetMapping("/bbs/notice")
    public String bbsNotice() {
        return "bbs/notice";
    }

    @GetMapping("/bbs/notice_detail")
    public String bbbsNoticeDetail() {
        return "bbs/notice_detail";
    }

    @GetMapping("/bbs/faq")
    public String bbsFaq() {
        return "bbs/faq";
    }

    @GetMapping("/bbs/event")
    public String bbsEvent() {
        return "bbs/event";
    }

    // menu
    @GetMapping("/menu/drink")
    public String menuDrink() {
        return "menu/drink";
    }

    @GetMapping("/menu/drink_detail")
    public String menuDrinkDetail() {
        return "menu/drink_detail";
    }

    // mypage
    @GetMapping("/mypage")
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

    @GetMapping("/mypage/inquiry_list")
    public String mypageInquiryList() {
        return "mypage/inquiry_list";
    }

    @GetMapping("/mypage/inquiry_detail")
    public String mypageInquiryDetail() {
        return "mypage/inquiry_detail";
    }

    // order
    @GetMapping("/order/cart")
    public String orderCart() {
        return "order/cart";
    }

    @GetMapping("/order/checkout")
    public String orderCheckout() {
        return "order/checkout";
    }

    @GetMapping("/order/detail")
    public String orderDetail() {
        return "order/detail";
    }

    @GetMapping("/order/history")
    public String orderHistory() {
        return "order/history";
    }

    // owner
    @GetMapping("/owner/order")
    public String ownerOrder() {
        return "owner/order";
    }

    @GetMapping("/owner/inventory")
    public String ownerInventory() {
        return "owner/inventory";
    }

    // policy
    @GetMapping("/policy/privacy_policy")
    public String policyPrivacy() {
        return "policy/privacy_policy";
    }

    @GetMapping("/policy/service_policy")
    public String policyService() {
        return "policy/service_policy";
    }
}