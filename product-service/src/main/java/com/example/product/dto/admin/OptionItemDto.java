package com.example.product.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OptionItemDto {

    private Integer optionId;
    private String optionName;
    private Integer defaultPrice;
}
