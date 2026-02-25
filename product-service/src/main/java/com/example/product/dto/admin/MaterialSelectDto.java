package com.example.product.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MaterialSelectDto {
    private Integer ingredientId;
    private String ingredientName;
    private String baseUnit;
}
