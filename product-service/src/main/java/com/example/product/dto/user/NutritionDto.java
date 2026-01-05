package com.example.product.dto.user;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter @Builder
public class NutritionDto {

    private BigDecimal calories;
    private BigDecimal sodium;
    private BigDecimal carbs;
    private BigDecimal sugars;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal saturatedFat;
    private BigDecimal caffeine;
}
