package com.example.product.dto.user;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter @Builder
public class OptionDto {

    private Integer optionId;
    private String optionName;
    private BigDecimal price;
}
