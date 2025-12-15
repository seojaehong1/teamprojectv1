package com.example.product.service;

import com.example.product.dto.AdminMenuRequestDto;
import com.example.product.dto.MenuDetailDto;
import com.example.product.model.*;
import com.example.product.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final NutritionRepository nutritionRepository;
    private final RecipeRepository recipeRepository;
    private final MenuOptionRepository menuOptionRepository;
    private final OptionMasterRepository optionMasterRepository;

    // 전체 메뉴 조회
    @Override
    public List<Menu> getAllMenus() {
        return menuRepository.findAll();
    }

    // 메뉴 상세 조회
    @Override
    public MenuDetailDto getMenuDetail(String menuCode) {

        Menu menu = menuRepository.findById(menuCode)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + menuCode));

        Nutrition nutrition = nutritionRepository.findById(menuCode).orElse(null);

        List<Recipe> recipeList = recipeRepository.findByMenuCode(menuCode);

        // menu_option 테이블에서 옵션 그룹 목록 조회
        List<MenuOption> menuOptions = menuOptionRepository.findByMenuCode(menuCode);

        List<String> optionGroupNames = menuOptions.stream()
                .map(MenuOption::getOptionGroupName)
                .distinct()
                .collect(Collectors.toList());

        // option_master에서 실제 옵션 조회
        List<OptionMaster> optionList = optionMasterRepository.findByOptionGroupNames(optionGroupNames);

        return MenuDetailDto.builder()
                .menuCode(menu.getMenuCode())
                .menuName(menu.getMenuName())
                .category(menu.getCategory())
                .basePrice(menu.getBasePrice())
                .baseVolume(menu.getBaseVolume())
                .allergyInfo(menu.getAllergyInfo())
                .description(menu.getDescription())
                .createTime(menu.getCreateTime())

                // Nutrition DTO 변환
                .nutrition(nutrition != null ?
                        MenuDetailDto.NutritionDto.builder()
                                .calories(nutrition.getCalories())
                                .sodium(nutrition.getSodium())
                                .carbs(nutrition.getCarbs())
                                .sugars(nutrition.getSugars())
                                .protein(nutrition.getProtein())
                                .fat(nutrition.getFat())
                                .saturatedFat(nutrition.getSaturatedFat())
                                .caffeine(nutrition.getCaffeine())
                                .build()
                        : null)

                // Recipe DTO 변환
                .recipeList(recipeList.stream()
                        .map(r -> MenuDetailDto.RecipeDto.builder()
                                .ingredientName(r.getMaterialMaster().getIngredientName())
                                .ingredientCategory(r.getIngredientCategory())
                                .requiredQuantity(r.getRequiredQuantity())
                                .unit(r.getUnit())
                                .build())
                        .collect(Collectors.toList()))

                // Option DTO 변환
                .optionList(optionList.stream()
                        .map(o -> MenuDetailDto.OptionDto.builder()
                                .optionId(o.getOptionId())
                                .optionGroupName(o.getOptionGroupName())
                                .optionName(o.getOptionName())
                                .defaultPrice(o.getDefaultPrice())
                                .build())
                        .collect(Collectors.toList()))

                .build();
    }

    // 관리자용 메뉴 등록
    @Override
    public Menu createMenu(AdminMenuRequestDto dto) {

        // 1) Menu 저장
        Menu menu = Menu.builder()
                .menuCode(dto.getMenuCode())
                .menuName(dto.getMenuName())
                .category(dto.getCategory())
                .basePrice(dto.getBasePrice())
                .baseVolume(dto.getBaseVolume())
                .allergyInfo(dto.getAllergyInfo())
                .description(dto.getDescription())
                .createTime(dto.getCreateTime())
                .build();

        menuRepository.save(menu);

        // 2) Nutrition 저장 (nullable 허용)
        if (dto.getNutrition() != null) {
            AdminMenuRequestDto.NutritionDto n = dto.getNutrition();

            Nutrition nutrition = Nutrition.builder()
                    .menuCode(dto.getMenuCode())
                    .calories(n.getCalories())
                    .sodium(n.getSodium())
                    .carbs(n.getCarbs())
                    .sugars(n.getSugars())
                    .protein(n.getProtein())
                    .fat(n.getFat())
                    .saturatedFat(n.getSaturatedFat())
                    .caffeine(n.getCaffeine())
                    .build();

            nutritionRepository.save(nutrition);
        }

        // 3) Recipe 저장
        if (dto.getRecipeList() != null) {
            dto.getRecipeList().forEach(r -> {
                Recipe recipe = Recipe.builder()
                        .menuCode(dto.getMenuCode())
                        .ingredientCategory(r.getIngredientCategory())
                        .requiredQuantity(r.getRequiredQuantity())
                        .unit(r.getUnit())
                        .materialMaster(
                                // FK로 재료명을 참조
                                MaterialMaster.builder()
                                        .ingredientName(r.getIngredientName())
                                        .build()
                        )
                        .build();

                recipeRepository.save(recipe);
            });
        }

        return menu;
    }

    // 관리자용 메뉴 수정
    @Override
    public Menu updateMenu(String menuCode, AdminMenuRequestDto dto) {

        Menu existing = menuRepository.findById(menuCode)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + menuCode));

        // Menu 데이터 수정
        existing.setMenuName(dto.getMenuName());
        existing.setCategory(dto.getCategory());
        existing.setBasePrice(dto.getBasePrice());
        existing.setBaseVolume(dto.getBaseVolume());
        existing.setAllergyInfo(dto.getAllergyInfo());
        existing.setDescription(dto.getDescription());
        existing.setCreateTime(dto.getCreateTime());
        menuRepository.save(existing);

        // Nutrition 수정 (없으면 생성)
        if (dto.getNutrition() != null) {

            Nutrition nutrition = Nutrition.builder()
                    .menuCode(menuCode)
                    .calories(dto.getNutrition().getCalories())
                    .sodium(dto.getNutrition().getSodium())
                    .carbs(dto.getNutrition().getCarbs())
                    .sugars(dto.getNutrition().getSugars())
                    .protein(dto.getNutrition().getProtein())
                    .fat(dto.getNutrition().getFat())
                    .saturatedFat(dto.getNutrition().getSaturatedFat())
                    .caffeine(dto.getNutrition().getCaffeine())
                    .build();

            nutritionRepository.save(nutrition);
        }

        // Recipe 먼저 모두 삭제
        recipeRepository.deleteByMenuCode(menuCode);

        // Recipe 다시 Insert
        if (dto.getRecipeList() != null) {
            dto.getRecipeList().forEach(r -> {
                Recipe recipe = Recipe.builder()
                        .menuCode(menuCode)
                        .ingredientCategory(r.getIngredientCategory())
                        .requiredQuantity(r.getRequiredQuantity())
                        .unit(r.getUnit())
                        .materialMaster(
                                MaterialMaster.builder()
                                        .ingredientName(r.getIngredientName())
                                        .build()
                        )
                        .build();

                recipeRepository.save(recipe);
            });
        }

        return existing;
    }


    // 관리자용 메뉴 삭제
    @Override
    public void deleteMenu(String menuCode) {

        // 레시피 삭제 (FK 때문에 먼저)
        recipeRepository.deleteByMenuCode(menuCode);

        // 영양 삭제
        nutritionRepository.deleteById(menuCode);

        // 메뉴 삭제
        menuRepository.deleteById(menuCode);
    }
}
