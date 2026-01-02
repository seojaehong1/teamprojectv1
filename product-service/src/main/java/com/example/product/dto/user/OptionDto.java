package com.example.product.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class OptionDto {

    private Integer optionId;
    private String optionName;
    private Integer price;
}
