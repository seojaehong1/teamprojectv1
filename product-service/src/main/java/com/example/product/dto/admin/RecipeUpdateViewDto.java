package com.example.product.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RecipeUpdateViewDto {

    private Integer ingredientId;
    private String ingredientName;
    private BigDecimal requiredQuantity;
    private String unit;
}
