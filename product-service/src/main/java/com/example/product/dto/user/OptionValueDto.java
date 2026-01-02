package com.example.product.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OptionValueDto {

    private Integer optionId;
    private String optionName;
    private Integer price;
}
