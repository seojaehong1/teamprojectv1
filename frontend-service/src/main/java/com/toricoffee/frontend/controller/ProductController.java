package com.toricoffee.frontend.controller;

import com.toricoffee.frontend.util.ApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/menu")
public class ProductController {

    @Autowired
    private ApiClient apiClient;

    @GetMapping("/drink")
    public String menuDrink() {
        return "menu/drink";
    }

    @GetMapping("/drink_detail")
    public String menuDrinkDetail() {
        return "menu/drink_detail";
    }
}