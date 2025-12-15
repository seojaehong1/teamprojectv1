package com.example.cust.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private String menuCode;
    private Integer quantity;
    private Integer priceAtOrder; // 주문 시점의 항목 기본 가격
    private Integer totalItemPrice; // 옵션 포함 해당 항목의 최종 가격
    private List<OrderOptionDto> orderOptions; // 해당 항목에 포함된 옵션 리스트
}