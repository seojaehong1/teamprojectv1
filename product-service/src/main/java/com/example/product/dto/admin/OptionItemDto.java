package com.example.product.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OptionItemDto {

    private Integer optionId;
    private String optionName;
    private BigDecimal defaultPrice;
}
