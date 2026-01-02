package com.example.product.dto.user;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MenuDetailDto {

    private String imageUrl;
    private String menuName;
    private String description;
    private String category;
    private Integer basePrice;
    private String baseVolume;

    private List<AllergyDto> allergies;   // allergy_ids 파싱 결과
    private NutritionDto nutrition;        // 없으면 null
}
