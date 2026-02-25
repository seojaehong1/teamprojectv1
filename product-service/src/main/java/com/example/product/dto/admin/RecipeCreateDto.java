package com.example.product.dto.admin;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class RecipeCreateDto {
    private Integer ingredientId;
    private BigDecimal requiredQuantity;
    private String unit;
}
