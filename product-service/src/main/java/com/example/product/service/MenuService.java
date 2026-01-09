package com.example.product.service;

import com.example.product.dto.user.AllergyDto;
import com.example.product.dto.user.MenuDetailDto;
import com.example.product.dto.user.MenuListDto;
import com.example.product.dto.user.NutritionDto;
import com.example.product.model.Menu;
import com.example.product.model.Nutrition;
import com.example.product.repository.AllergyRepository;
import com.example.product.repository.MenuRepository;
import com.example.product.repository.NutritionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final NutritionRepository nutritionRepository;
    private final AllergyRepository allergyRepository;

    // 1. 메뉴 목록 조회 [GET /api/menu/drinks]
    public Page<MenuListDto> getMenuList(
            List<String> categories,
            String keyword,
            int page,
            int limit
    ) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Menu> menus;

        boolean hasCategory = categories != null && !categories.isEmpty();
        boolean hasKeyword  = keyword != null && !keyword.isBlank();

        if (hasCategory && hasKeyword) {
            menus = menuRepository.findByCategoryInAndMenuNameContainingIgnoreCase(categories, keyword, pageable);
        } else if (hasCategory) {
            menus = menuRepository.findByCategoryIn(categories, pageable);
        } else if (hasKeyword) {
            menus = menuRepository.findByMenuNameContainingIgnoreCase(keyword, pageable);
        } else {
            menus = menuRepository.findAll(pageable);
        }

        return menus.map(menu -> MenuListDto.builder()
                .menuCode(menu.getMenuCode())
                .menuName(menu.getMenuName())
                .description(menu.getDescription())
                .category(menu.getCategory())
                .build());
    }


    // 2. 메뉴 검색 [GET /api/menu/drinks/search]
    public Page<MenuListDto> searchMenus(String keyword, int page, int limit) {

        if (keyword == null || keyword.isBlank()) { // 검색어 필수 처리
            throw new IllegalArgumentException("검색어를 입력해 주세요.");
        }

        int safePage = Math.max(page, 1); // page 음수 처리 방어
        int safeLimit = Math.max(limit, 1); // page 음수 처리 방어

        Pageable pageable = PageRequest.of(safePage - 1, safeLimit);

        Page<Menu> menus = menuRepository.findByMenuNameContaining(keyword, pageable);

        return menus.map(menu ->
                MenuListDto.builder()
                        .menuCode(menu.getMenuCode())
                        .imageUrl(buildImageUrl(menu.getMenuCode()))
                        .menuName(menu.getMenuName())
                        .description(menu.getDescription())
                        .category(menu.getCategory())
                        .build()
        );
    }

    // 3. 메뉴 상세 조회 [GET /api/menu/drinks/{menuCode}]
    public MenuDetailDto getMenuDetail(Long menuCode) {

        Menu menu = menuRepository.findById(menuCode)
                .orElseThrow(() ->
                        new IllegalArgumentException("메뉴를 찾을 수 없습니다.")
                );

        // 1. 알레르기 정보 파싱
        List<AllergyDto> allergies = parseAllergies(menu.getAllergyIds());

        // 2. 영양 정보 조회 (없을 수도 있음)
        Nutrition nutrition = nutritionRepository
                .findById(menuCode)
                .orElse(null);

        NutritionDto nutritionDto = nutrition == null ? null :
                NutritionDto.builder()
                        .calories(nutrition.getCalories())
                        .sodium(nutrition.getSodium())
                        .carbs(nutrition.getCarbs())
                        .sugars(nutrition.getSugars())
                        .protein(nutrition.getProtein())
                        .fat(nutrition.getFat())
                        .saturatedFat(nutrition.getSaturatedFat())
                        .caffeine(nutrition.getCaffeine())
                        .build();

        return MenuDetailDto.builder()
                .menuCode(menu.getMenuCode())
                .imageUrl(buildImageUrl(menu.getMenuCode()))
                .menuName(menu.getMenuName())
                .description(menu.getDescription())
                .category(menu.getCategory())
                .basePrice(menu.getBasePrice())
                .baseVolume(menu.getBaseVolume())
                .allergies(allergies)
                .nutrition(nutritionDto)
                .build();
    }

    // 3.1 알레르기 문자열 파싱
    private List<AllergyDto> parseAllergies(String allergyIds) {

        if (allergyIds == null || allergyIds.isBlank()) {
            return List.of();
        }

        List<Integer> ids = Arrays.stream(allergyIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank()) // 잘못된 토큰 필터 처리
                .map(Integer::parseInt)
                .distinct()
                .toList();

        return allergyRepository.findAllById(ids).stream()
                .map(a -> new AllergyDto(a.getAllergyId(), a.getAllergyName()))
                .collect(Collectors.toList());
    }

    private String buildImageUrl(Long menuCode) {
        return "/images/menu/" + menuCode + ".jpg";
    }

}
