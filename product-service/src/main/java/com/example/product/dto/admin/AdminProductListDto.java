package com.example.product.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminProductListDto {

    private String imageUrl;   // /images/menu/{menuCode}.jpg
    private Integer menuCode;
    private String menuName;
    private String category;
    private Integer basePrice;
}
