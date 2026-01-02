package com.example.product.service.admin;

import com.example.product.dto.admin.AdminProductCreateUpdateDto;
import com.example.product.dto.admin.AdminProductListDto;
import com.example.product.model.*;
import com.example.product.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final MenuRepository menuRepository;
    private final NutritionRepository nutritionRepository;
    private final MenuOptionRepository menuOptionRepository;
    private final OptionMasterRepository optionMasterRepository;


    // 관리자 제품 목록 조회 [GET /api/admin/products]
    public Page<AdminProductListDto> getProductList(int page, int limit) {

        Pageable pageable = PageRequest.of(page - 1, limit);

        return menuRepository.findAll(pageable)
                .map(menu -> AdminProductListDto.builder()
                        .imageUrl(buildImageUrl(menu.getMenuCode()))
                        .menuCode(menu.getMenuCode())
                        .menuName(menu.getMenuName())
                        .category(menu.getCategory())
                        .basePrice(menu.getBasePrice())
                        .build()
                );
    }

    // 관리자 제품 등록 [POST /api/admin/products]
    @Transactional
    public void createProduct(AdminProductCreateUpdateDto dto) {

        // 1. menu 저장
        Menu menu = Menu.builder()
                .menuName(dto.getMenuName())
                .category(dto.getCategory())
                .basePrice(dto.getBasePrice())
                .baseVolume(dto.getBaseVolume())
                .description(dto.getDescription())
                .createTime(dto.getCreateTime())
                .allergyIds(joinAllergyIds(dto.getAllergyIds()))
                .build();

        Menu savedMenu = menuRepository.save(menu);

        // 2. nutrition 저장
        Nutrition nutrition = Nutrition.builder()
                .menu(savedMenu)   // @MapsId
                .calories(dto.getCalories())
                .sodium(dto.getSodium())
                .carbs(dto.getCarbs())
                .sugars(dto.getSugars())
                .protein(dto.getProtein())
                .fat(dto.getFat())
                .saturatedFat(dto.getSaturatedFat())
                .caffeine(dto.getCaffeine())
                .build();

        nutritionRepository.save(nutrition);

        // 3. 옵션 매칭 저장
        saveMenuOptions(savedMenu, dto.getOptionIds());
    }


    // 관리자 제품 수정 [PUT /api/admin/products/{menuCode}]
    @Transactional
    public void updateProduct(Integer menuCode, AdminProductCreateUpdateDto dto) {

        Menu menu = menuRepository.findById(menuCode)
                .orElseThrow(() ->
                        new IllegalArgumentException("수정할 제품을 찾을 수 없습니다.")
                );

        // 1. menu 수정
        menu.setMenuName(dto.getMenuName());
        menu.setCategory(dto.getCategory());
        menu.setBasePrice(dto.getBasePrice());
        menu.setBaseVolume(dto.getBaseVolume());
        menu.setDescription(dto.getDescription());
        menu.setCreateTime(dto.getCreateTime());
        menu.setAllergyIds(joinAllergyIds(dto.getAllergyIds()));

        // 2. nutrition 수정
        Nutrition nutrition = nutritionRepository
                .findById(String.valueOf(menuCode))
                .orElse(
                        Nutrition.builder()
                                .menu(menu)
                                .build()
                );

        nutrition.setCalories(dto.getCalories());
        nutrition.setSodium(dto.getSodium());
        nutrition.setCarbs(dto.getCarbs());
        nutrition.setSugars(dto.getSugars());
        nutrition.setProtein(dto.getProtein());
        nutrition.setFat(dto.getFat());
        nutrition.setSaturatedFat(dto.getSaturatedFat());
        nutrition.setCaffeine(dto.getCaffeine());

        nutritionRepository.save(nutrition);

        // 3. 기존 옵션 매핑 제거 후 재설정
        menuOptionRepository.deleteByMenu_MenuCode(menuCode);
        saveMenuOptions(menu, dto.getOptionIds());
    }

    // 관리자 제품 삭제 [DELETE /api/admin/products/{menuCode}]
    @Transactional
    public void deleteProduct(Integer menuCode) {

        if (!menuRepository.existsById(menuCode)) {
            throw new IllegalArgumentException("삭제할 제품을 찾을 수 없습니다.");
        }

        /* 연관 데이터 삭제 */
        menuOptionRepository.deleteByMenu_MenuCode(menuCode);
        nutritionRepository.deleteById(String.valueOf(menuCode));

        menuRepository.deleteById(menuCode);
    }

    // 옵션 매핑 저장 (공통)
    private void saveMenuOptions(Menu menu, List<Integer> optionIds) {

        if (optionIds == null || optionIds.isEmpty()) {
            return;
        }

        List<OptionMaster> options =
                optionMasterRepository.findAllById(optionIds);

        options.stream()
                .map(OptionMaster::getOptionGroupName)
                .distinct()
                .forEach(groupName -> {

                    MenuOption menuOption = MenuOption.builder()
                            .menu(menu)
                            .optionGroupName(groupName)
                            .build();

                    menuOptionRepository.save(menuOption);
                });
    }

    // 알레르기 ID 리스트 → 문자열 변환
    private String joinAllergyIds(List<Integer> allergyIds) {

        if (allergyIds == null || allergyIds.isEmpty()) {
            return null;
        }

        return allergyIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    // 이미지 경로 생성
    private String buildImageUrl(Integer menuCode) {
        return "/images/menu/" + menuCode + ".jpg";
    }
}