package com.example.product.dto;
// 혹은 order-service에서도 동일한 구조로 정의

import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionDto {

    // 1. 옵션 식별자 (OptionMaster.id)
    private Integer optionId;     // 옵션 ID (option_id)

    // 2. 옵션 이름 (사용자에게 표시/저장)
    private String optionName;    // 옵션 이름 (option_name)

    private Integer optionPrice; // 옵션 가격 (option_price)

    private String optionGroupName; // 옵션 그룹 이름
}