package com.example.product.controller;


import com.example.product.dto.OptionDto; // OptionDto 임포트 추가
import com.example.product.model.Menu;
import com.example.product.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// 맵핑을 위한 Map, List, Stream 임포트 추가
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // 1. 메인 페이지 (Home)
    @GetMapping
    public String home() {
        return "home";
    }

    // 2. 상품 목록 페이지 (Menu)
    @GetMapping("/menu")
    public String menuList(@RequestParam(required = false) String category, Model model) {

        // 1. 카테고리 네비게이션 데이터 로드
        List<String> categories = menuService.getAllCategories();
        model.addAttribute("categories", categories);

        // 2. 메뉴 목록 로드 및 필터링 적용
        List<Menu> menus;

        // 'category' 파라미터가 유효하고 '전체'가 아닐 경우 필터링
        if (category != null && !category.isEmpty() && !category.equalsIgnoreCase("전체")) {
            // Service에서 해당 카테고리의 메뉴만 가져옴
            menus = menuService.getMenusByCategory(category);
        } else {
            // 파라미터가 없거나 '전체'일 경우 모든 메뉴를 가져옴
            menus = menuService.getAllMenus();
        }

        model.addAttribute("menus", menus);
        model.addAttribute("selectedCategory", category); // 현재 선택된 카테고리 표시용

        // 🌟 3. 옵션 데이터 로드 및 그룹별 맵핑 로직 추가
        try {
            // Service에서 모든 옵션 DTO를 가져옵니다.
            List<OptionDto> allOptions = menuService.getAllOptions();

            // 옵션 그룹 이름(optionGroupName)을 기준으로 Map으로 맵핑(그룹화)합니다.
            // Map: Key=옵션 그룹명 (String), Value=해당 그룹의 OptionDto 리스트
            Map<String, List<OptionDto>> optionsByGroup = allOptions.stream()
                    .collect(Collectors.groupingBy(OptionDto::getOptionGroupName));

            model.addAttribute("optionsByGroup", optionsByGroup);

        } catch (Exception e) {
            // 옵션 로드 중 오류 발생 시 처리 (예: DB 연결 오류 등)
            System.err.println("옵션 데이터 로드 중 오류 발생: " + e.getMessage());
            model.addAttribute("optionsByGroup", Map.of()); // 빈 맵을 전달하여 오류 회피
        }

        return "menu-list";
    }
    @GetMapping("/api/menu/{menuCode}/options")
    @ResponseBody // Map을 JSON 형태로 반환합니다.
    public Map<String, List<OptionDto>> getMenuOptions(@PathVariable String menuCode) {

        // 1. Service를 통해 해당 메뉴 코드에 허용된 옵션 그룹의 상세 옵션을 모두 가져옵니다.
        List<OptionDto> filteredOptions = menuService.getOptionsByMenuCode(menuCode);

        // 2. 결과를 옵션 그룹 이름으로 Map핑하여 JSON 형태로 반환합니다.
        Map<String, List<OptionDto>> optionsByGroup = filteredOptions.stream()
                .collect(Collectors.groupingBy(OptionDto::getOptionGroupName));

        return optionsByGroup;
    }

}