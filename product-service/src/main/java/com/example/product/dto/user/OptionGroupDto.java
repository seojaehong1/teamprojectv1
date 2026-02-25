package com.example.product.dto.user;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OptionGroupDto {

    private String optionGroupName;
    private List<OptionValueDto> options;
}
