package com.example.product.controller.admin;

import com.example.product.dto.admin.OptionGroupDto;
import com.example.product.service.admin.AdminOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/options")
@RequiredArgsConstructor
public class AdminOptionController {

    private final AdminOptionService adminOptionService;

    // 1. create 단계 option selector
    @GetMapping
    public List<OptionGroupDto> getAllOptions() {
        return adminOptionService.getAllOptionsGrouped();
    }

}
