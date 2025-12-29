package com.toricoffee.frontend.controller;

import com.toricoffee.frontend.util.ApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private ApiClient apiClient;

    @GetMapping("/cart")
    public String orderCart() {
        return "order/cart";
    }

    @GetMapping("/checkout")
    public String orderCheckout() {
        return "order/checkout";
    }

    @GetMapping("/detail")
    public String orderDetail() {
        return "order/detail";
    }

    @GetMapping("/history")
    public String orderHistory() {
        return "order/history";
    }
}