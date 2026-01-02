package com.toricoffee.frontend.controller;

import com.toricoffee.frontend.util.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/owner")
public class OwnerController {

    @Autowired
    private ApiClient apiClient;

    @GetMapping("/order")
    public String orderCheckout() {
        return "owner/order";
    }
}
