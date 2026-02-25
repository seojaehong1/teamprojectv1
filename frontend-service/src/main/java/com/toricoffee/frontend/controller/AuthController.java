package com.toricoffee.frontend.controller;

import com.toricoffee.frontend.util.ApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @Autowired
    private ApiClient apiClient;

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
}