package com.example.product.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AllergySelectDto {
    private Integer allergyId;
    private String allergyName;
}