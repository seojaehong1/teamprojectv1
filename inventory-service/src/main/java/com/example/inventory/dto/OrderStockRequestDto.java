package com.example.inventory.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStockRequestDto { // Order-service로부터 주문 정보를 받는 DTO

    private Integer orderId; // 주문 번호

    private List<OrderItemDto> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemDto {
        private Long menuCode;
        private Integer quantity;
        private List<Integer> optionIds;
    }

}
