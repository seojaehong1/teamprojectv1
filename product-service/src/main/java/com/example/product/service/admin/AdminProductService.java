package com.example.product.service.admin;

import com.example.product.dto.admin.*;
import com.example.product.model.*;
import com.example.product.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final MenuRepository menuRepository;
    private final NutritionRepository nutritionRepository;
    private final MenuOptionRepository menuOptionRepository;
    private final OptionMasterRepository optionMasterRepository;
    private final MaterialMasterRepository materialMasterRepository;
    private final RecipeRepository recipeRepository;


    // 관리자 제품 목록 조회 [GET /api/admin/products]
    public Page<AdminProductListDto> getProductList(int page, int limit) {

        int safePage = Math.max(page, 1); // page 음수 처리 방어
        int safeLimit = Math.max(limit, 1); // page 음수 처리 방어

        Pageable pageable = PageRequest.of(safePage - 1, safeLimit);

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

    // 관리자 제품 검색 [GET /api/admin/products/search] ** 01.05 update
    public Page<AdminProductListDto> searchProducts(String keyword, int page, int limit) {

        if (keyword == null || keyword.isBlank()) { // 검색어 필수 처리
            throw new IllegalArgumentException("검색어는 필수입니다.");
        }

        int safePage = Math.max(page, 1); // page 음수 처리 방어
        int safeLimit = Math.max(limit, 1); // page 음수 처리 방어

        Pageable pageable = PageRequest.of(safePage - 1, safeLimit);

        return menuRepository.findByMenuNameContaining(keyword, pageable)
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
        Menu menu = menuRepository.save(
                Menu.builder()
                        .menuName(dto.getMenuName())
                        .category(dto.getCategory())
                        .basePrice(dto.getBasePrice())
                        .baseVolume(dto.getBaseVolume())
                        .description(dto.getDescription())
                        .createTime(dto.getCreateTime())
                        .allergyIds(joinAllergyIds(dto.getAllergyIds()))
                        .isAvailable(true)
                        .build()
        );

        // 2. nutrition 저장
        nutritionRepository.save(
                Nutrition.builder()
                        .menu(menu)   // @MapsId
                        .calories(dto.getCalories())
                        .sodium(dto.getSodium())
                        .carbs(dto.getCarbs())
                        .sugars(dto.getSugars())
                        .protein(dto.getProtein())
                        .fat(dto.getFat())
                        .saturatedFat(dto.getSaturatedFat())
                        .caffeine(dto.getCaffeine())
                        .build()
        );

        // 3. 옵션 매칭 저장
        saveMenuOptions(menu, dto.getOptionIds());

        // 4. recipe 저장
        saveRecipes(menu, dto.getRecipes());
    }

    // 관리자 제품 수정 화면용 조회 [GET /api/admin/products/{menuCode}]
    @Transactional
    public AdminProductUpdateViewDto getProductForUpdate(Long menuCode) {

        // 1. menu 조회
        Menu menu = menuRepository.findById(menuCode)
                .orElseThrow(() ->
                        new IllegalArgumentException("수정할 제품을 찾을 수 없습니다.")
                );

        // 2. allergyIds 파싱 ("1, 2, 5" -> List<Integer>)
        List<Integer> allergyIds = parseAllergyIds(menu.getAllergyIds()); //함수 필요

        // 3. nutrition 조회
        Nutrition nutrition = nutritionRepository
                .findByMenu_MenuCode(menuCode)
                .orElse(null);

        // 4. options Ids 조회
        List<Integer> optionIds = List.of();

        // 5. recipe 조회
        List<RecipeUpdateViewDto> recipes = recipeRepository
                .findByMenu_MenuCode(menuCode)
                .stream()
                .map(recipe -> RecipeUpdateViewDto.builder()
                        .ingredientId(recipe.getMaterialMaster().getIngredientId())
                        .ingredientName(recipe.getMaterialMaster().getIngredientName())
                        .requiredQuantity(recipe.getRequiredQuantity())
                        .unit(recipe.getUnit())
                        .build()
                )
                .collect(Collectors.toList());

        // 6. DTO 조립
        return AdminProductUpdateViewDto.builder()
                .menuCode(menu.getMenuCode())
                .menuName(menu.getMenuName())
                .category(menu.getCategory())
                .basePrice(menu.getBasePrice())
                .baseVolume(menu.getBaseVolume())
                .description(menu.getDescription())
                .createTime(menu.getCreateTime())

                .allergyIds(allergyIds)

                .calories(nutrition != null ? nutrition.getCalories() : null)
                .sodium(nutrition != null ? nutrition.getSodium() : null)
                .carbs(nutrition != null ? nutrition.getCarbs() : null)
                .sugars(nutrition != null ? nutrition.getSugars() : null)
                .protein(nutrition != null ? nutrition.getProtein() : null)
                .fat(nutrition != null ? nutrition.getFat() : null)
                .saturatedFat(nutrition != null ? nutrition.getSaturatedFat() : null)
                .caffeine(nutrition != null ? nutrition.getCaffeine() : null)

                .optionIds(optionIds)
                .recipes(recipes)
                .build();
    }


    // 관리자 제품 수정 [PUT /api/admin/products/{menuCode}]
    @Transactional
    public void updateProduct(Long menuCode, AdminProductCreateUpdateDto dto) {

        // 0. 기존 메뉴 조회
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
        Nutrition nutrition = nutritionRepository.findById(menuCode)
                .orElseGet(() ->
                        Nutrition.builder()
                                .menu(menu)   // @MapsId
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

        // 4. 레시피 재설정
        recipeRepository.deleteByMenu_MenuCode(menuCode);
        saveRecipes(menu, dto.getRecipes());
    }

    // 관리자 제품 삭제 [DELETE /api/admin/products/{menuCode}]
    @Transactional
    public void deleteProduct(Long menuCode) {

        // 0. 메뉴 존재 여부 확인
        Menu menu = menuRepository.findById(menuCode)
                .orElseThrow(() ->
                        new IllegalArgumentException("삭제할 제품을 찾을 수 없습니다.")
                );

        // 1. recipe 삭제
        recipeRepository.deleteByMenu_MenuCode(menuCode);

        // 2. menu_option 삭제
        menuOptionRepository.deleteByMenu_MenuCode(menuCode);

        // 3. nutrition 삭제
        nutritionRepository.deleteByMenu_MenuCode(menuCode);

        // 4. menu 삭제
        menuRepository.deleteById(menuCode);
    }

    // 옵션 매핑 저장 (공통)
    private void saveMenuOptions(Menu menu, List<Integer> optionIds) {

        if (optionIds == null || optionIds.isEmpty()) return;

        optionMasterRepository.findAllById(optionIds)
                .forEach(option ->
                        menuOptionRepository.save(
                                MenuOption.builder()
                                        .menu(menu)
                                        .optionGroupName(option.getOptionGroupName())
                                        .build()
                        )
                );
    }

    // recipe 저장
    private void saveRecipes(Menu menu, List<RecipeCreateDto> recipes) {

        if (recipes == null || recipes.isEmpty()) return;

        for (RecipeCreateDto dto : recipes) {

            MaterialMaster material = materialMasterRepository.findById(dto.getIngredientId()).orElseThrow(() ->
                    new IllegalArgumentException("존재하지 않는 원재료 ID: " + dto.getIngredientId()));

            recipeRepository.save(
                    Recipe.builder()
                            .menu(menu)
                            .materialMaster(material)
                            .requiredQuantity(dto.getRequiredQuantity())
                            .unit(dto.getUnit())
                            .build()
            );
        }
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

    // "1, 2, 5" -> List<Integer>
    private List<Integer> parseAllergyIds(String allergyIds) {
        if (allergyIds == null || allergyIds.isBlank()) {
            return List.of();
        }

        return Arrays.stream(allergyIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank()) // 잘못된 토큰 필터 처리
                .map(Integer::parseInt)
                .distinct()
                .toList();
    }

    // 이미지 경로 생성
    private String buildImageUrl(Long menuCode) {
        return "/images/menu/" + menuCode + ".jpg";
    }
}