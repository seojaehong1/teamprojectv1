package com.example.product.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuDetailDto {

    //메뉴 기본 정보
    private String menuCode;
    private String menuName;
    private String category;
    private Integer basePrice;
    private String baseVolume;
    private String allergyInfo;
    private String description;
    private Integer createTime;

    // 영양 정보
    private NutritionDto nutrition;

    // 레시피 정보
    private List<RecipeDto> recipeList;

    // 옵션 정보
    private List<OptionDto> optionList;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NutritionDto {
        private Double calories;
        private Double sodium;
        private Double carbs;
        private Double sugars;
        private Double protein;
        private Double fat;
        private Double saturatedFat;
        private Double caffeine;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecipeDto {
        private String ingredientName;
        private String ingredientCategory;
        private BigDecimal requiredQuantity;
        private String unit;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OptionDto {
        private Integer optionId;
        private String optionGroupName;
        private String optionName;
        private Integer defaultPrice;
    }
}
