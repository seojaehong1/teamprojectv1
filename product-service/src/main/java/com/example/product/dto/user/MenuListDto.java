package com.example.product.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class MenuListDto {

    private Long menuCode;
    private String imageUrl; // /images/menu/{menuCode}.jpg
    private String menuName;
    private String description;
}
