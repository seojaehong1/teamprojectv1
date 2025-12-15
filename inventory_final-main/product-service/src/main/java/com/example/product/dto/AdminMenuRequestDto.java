package com.example.product.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminMenuRequestDto {

    private String menuCode;
    private String menuName;
    private String category;
    private Integer basePrice;
    private String baseVolume;
    private String allergyInfo;
    private String description;
    private Integer createTime;

    private NutritionDto nutrition;

    private List<RecipeDto> recipeList;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
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

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RecipeDto {
        private String ingredientName;
        private String ingredientCategory;
        private BigDecimal requiredQuantity;
        private String unit;
    }

}
