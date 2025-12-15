package com.example.product.controller;

import com.example.product.dto.AdminMenuRequestDto;
import com.example.product.dto.MenuDetailDto;
import com.example.product.model.Menu;
import com.example.product.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    //전체 메뉴 조회
    @GetMapping
    public ResponseEntity<List<Menu>> getAllMenus() {
        return ResponseEntity.ok(menuService.getAllMenus());
    }

    //메뉴 상세 조회
    @GetMapping("/{menuCode}")
    public ResponseEntity<MenuDetailDto> getMenuDetail(@PathVariable String menuCode) {
        MenuDetailDto menuDetailDto = menuService.getMenuDetail(menuCode);
        menuDetailDto.setRecipeList(null);
        return ResponseEntity.ok(menuDetailDto);
    }

    // 메뉴 등록 (관리자용)
    @PostMapping
    public ResponseEntity<Menu> createMenu(@RequestBody AdminMenuRequestDto dto) {
        Menu createdMenu = menuService.createMenu(dto);
        return ResponseEntity.ok(createdMenu);
    }

    // 메뉴 수정 (관리자용)
    @PutMapping("/{menuCode}")
    public ResponseEntity<Menu> updateMenu(
            @PathVariable String menuCode,
            @RequestBody AdminMenuRequestDto dto
    ) {
        Menu updatedMenu = menuService.updateMenu(menuCode, dto);
        return ResponseEntity.ok(updatedMenu);
    }

    // 메뉴 삭제 (관리자용)
    @DeleteMapping("/{menuCode}")
    public ResponseEntity<Void> deleteMenu(@PathVariable String menuCode) {
        menuService.deleteMenu(menuCode);
        return ResponseEntity.noContent().build();
    }
}
