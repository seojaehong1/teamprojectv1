package com.example.product.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AdminProductListDto {

    private String imageUrl;   // /images/menu/{menuCode}.jpg
    private Long menuCode;
    private String menuName;
    private String category;
    private BigDecimal basePrice;
}
