package com.example.product.dto.admin;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class AdminProductCreateUpdateDto {

    // 1. menu
    private String menuName;
    private String category;
    private BigDecimal basePrice;
    private String baseVolume;
    private String description;
    private Integer createTime;

    // 2. allergy
    private List<Integer> allergyIds;

    // 3. nutrition
    private BigDecimal calories;
    private BigDecimal sodium;
    private BigDecimal carbs;
    private BigDecimal sugars;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal saturatedFat;
    private BigDecimal caffeine;

    // 4. option
    private List<Integer> optionIds; // option_master PK 목록

    // 5. recipe
    private List<RecipeCreateDto> recipes;
}
