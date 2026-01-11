package com.example.product.controller.admin;

import com.example.product.dto.admin.MaterialSelectDto;
import com.example.product.service.admin.AdminMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminMaterialController {

    private final AdminMaterialService adminMaterialService;

    // 1. create 단계 material_master selector
    @GetMapping("/materials")
    public List<MaterialSelectDto> getMaterials() {
        return adminMaterialService.getAllMaterials();
    }
}
