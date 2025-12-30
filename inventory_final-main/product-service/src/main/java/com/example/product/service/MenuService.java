package com.example.product.service;

import com.example.product.dto.AdminMenuRequestDto;
import com.example.product.dto.MenuDetailDto;
import com.example.product.model.Menu;

import java.util.List;

public interface MenuService {

    // 1. 사용자용: 전체 메뉴 조회
    List<Menu> getAllMenus();

    // 2. 사용자용: 상세 조회 (레시피는 프론트에서 숨김)
    MenuDetailDto getMenuDetail(String menuCode);

    // 3. 관리자용: 메뉴 등록 + 영양 + 레시피 처리
    Menu createMenu(AdminMenuRequestDto dto);

    // 4. 관리자용: 메뉴 수정 + 영양 + 레시피 수정
    Menu updateMenu(String menuCode, AdminMenuRequestDto dto);

    // 5. 관리자용: 메뉴 삭제
    void deleteMenu(String menuCode);
}
