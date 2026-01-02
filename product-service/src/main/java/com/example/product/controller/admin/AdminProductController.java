package com.example.product.controller.admin;

import com.example.product.dto.admin.AdminProductCreateUpdateDto;
import com.example.product.dto.admin.AdminProductListDto;
import com.example.product.service.admin.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    // 1. 관리자 제품 목록 조회 GET /api/admin/products
    @GetMapping
    public Page<AdminProductListDto> getProductList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return adminProductService.getProductList(page, limit);
    }

    // 2. 관리자 제품 등록 [POST /api/admin/products]
    @PostMapping
    public void createProduct(
            @RequestBody AdminProductCreateUpdateDto dto
    ) {
        adminProductService.createProduct(dto);
    }

    // 3. 관리자 제품 수정 [PUT /api/admin/products/{menuCode}]
    @PutMapping("/{menuCode}")
    public void updateProduct(
            @PathVariable Integer menuCode,
            @RequestBody AdminProductCreateUpdateDto dto
    ) {
        adminProductService.updateProduct(menuCode, dto);
    }

    // 4. 관리자 제품 삭제 DELETE /api/admin/products/{menuCode}
    @DeleteMapping("/{menuCode}")
    public void deleteProduct(
            @PathVariable Integer menuCode
    ) {
        adminProductService.deleteProduct(menuCode);
    }
}
