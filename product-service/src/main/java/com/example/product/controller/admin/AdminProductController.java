package com.example.product.controller.admin;

import com.example.product.dto.admin.AdminProductCreateUpdateDto;
import com.example.product.dto.admin.AdminProductListDto;
import com.example.product.dto.admin.AdminProductUpdateViewDto;
import com.example.product.service.admin.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    // 1. 관리자 제품 목록 조회 [GET /api/admin/products]
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
            @PathVariable Long menuCode,
            @RequestBody AdminProductCreateUpdateDto dto
    ) {
        adminProductService.updateProduct(menuCode, dto);
    }

    // 3-1. 관리자 제품 수정을 위한 조회 [GET /api/admin/products/{menuCode}]
    @GetMapping("/{menuCode}")
    public AdminProductUpdateViewDto getProductForUpdate(
            @PathVariable Long menuCode
    ) {
        return adminProductService.getProductForUpdate(menuCode);
    }

    // 4. 관리자 제품 삭제 [DELETE /api/admin/products/{menuCode}]
    @DeleteMapping("/{menuCode}")
    public Map<String, String> deleteProduct(
            @PathVariable Long menuCode
    ) {
        adminProductService.deleteProduct(menuCode);
        return Map.of("message", "제품이 삭제되었습니다.");
    }

    // 5. 관리자 제품 검색 [GET /api/admin/products/search] ** 01.05 update
    @GetMapping("/search")
    public Page<AdminProductListDto> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return adminProductService.searchProducts(keyword, page, limit);
    }
}
