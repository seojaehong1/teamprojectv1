package com.example.product.controller.admin;

import com.example.product.dto.admin.AllergySelectDto;
import com.example.product.service.admin.AdminAllergyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/allergies")
@RequiredArgsConstructor
public class AdminAllergyController {

    private final AdminAllergyService adminAllergyService;

    // 1. create 단계 allergy selector
    @GetMapping
    public List<AllergySelectDto> getAllergies() {
        return adminAllergyService.getAllAllergies();
    }
}
