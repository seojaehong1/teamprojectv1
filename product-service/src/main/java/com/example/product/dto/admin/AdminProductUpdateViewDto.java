package com.example.product.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class AdminProductUpdateViewDto {

    // menu
    private Integer menuCode;
    private String menuName;
    private String category;
    private Integer basePrice;
    private String baseVolume;
    private String description;
    private Integer createTime;

    // allergy
    private List<Integer> allergyIds;

    // nutrition
    private BigDecimal calories;
    private BigDecimal sodium;
    private BigDecimal carbs;
    private BigDecimal sugars;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal saturatedFat;
    private BigDecimal caffeine;

    // option
    private List<Integer> optionIds;

    // recipe
    private List<RecipeUpdateViewDto> recipes;
}
