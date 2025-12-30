package com.example.product.controller;

import com.example.product.dto.ProductDto;
import com.example.product.model.Menu;
import com.example.product.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class    ProductController {

    private final MenuService menuService;

    /**
     * 모든 상품(메뉴) 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<Menu> menus = menuService.getAllMenus();
        List<ProductDto> products = menus.stream()
                .map(this::convertToProductDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    /**
     * 특정 상품(메뉴) 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable String id) {
        return menuService.getMenuById(id)
                .map(menu -> ResponseEntity.ok(convertToProductDto(menu)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 상품(메뉴) 추가
     */
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        // TODO: Menu 엔티티로 변환하여 저장하는 로직 구현 필요
        // 현재는 간단히 반환만 함
        return ResponseEntity.ok(productDto);
    }

    /**
     * 상품(메뉴) 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable String id, @RequestBody ProductDto productDto) {
        // TODO: Menu 엔티티 업데이트 로직 구현 필요
        // 현재는 간단히 반환만 함
        return ResponseEntity.ok(productDto);
    }

    /**
     * 상품(메뉴) 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        // TODO: Menu 엔티티 삭제 로직 구현 필요
        return ResponseEntity.ok().build();
    }

    /**
     * Menu 엔티티를 ProductDto로 변환
     */
    private ProductDto convertToProductDto(Menu menu) {
        return ProductDto.builder()
                .id(menu.getMenuCode())
                .name(menu.getMenuName())
                .description(menu.getCategory() != null ? menu.getCategory() : "")
                .price(menu.getBasePrice())
                .stock(0) // 재고는 기본값 0 (필요시 추가 필드 구현)
                .build();
    }
}

