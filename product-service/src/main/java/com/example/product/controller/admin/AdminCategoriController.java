package com.example.product.controller.admin;

import com.example.product.dto.admin.CategoriSelectDto;
import com.example.product.service.admin.AdminCategoriService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categori")
@RequiredArgsConstructor
public class AdminCategoriController {

    private final AdminCategoriService adminCategoriService;

    @GetMapping
    public List<CategoriSelectDto> getCategories() {
        return adminCategoriService.getAllCategories();
    }
}