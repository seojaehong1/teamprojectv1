package com.example.cust.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderOptionDto {
    private Integer optionId;
    private Integer optionPriceAtOrder; // 주문 시점의 개별 옵션 가격
}