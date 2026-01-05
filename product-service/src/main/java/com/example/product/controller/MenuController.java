package com.example.product.controller;

import com.example.product.dto.user.MenuDetailDto;
import com.example.product.dto.user.MenuListDto;
import com.example.product.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/menu/drinks")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // 1. 메뉴 목록 조회
    @GetMapping
    public Page<MenuListDto> getMenuList(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return menuService.getMenuList(category, page, limit);
    }

    // 2. 메뉴 검색
    @GetMapping("/search")
    public Page<MenuListDto> searchMenu(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return menuService.searchMenus(keyword, page, limit);
    }

    // 3. 메뉴 상세 조회
    @GetMapping("/{menuCode}")
    public MenuDetailDto getMenuDetail(@PathVariable Long menuCode) {
        return menuService.getMenuDetail(menuCode);
    }
}
