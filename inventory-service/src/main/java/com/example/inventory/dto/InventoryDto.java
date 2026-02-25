package com.example.inventory.dto;

import lombok.*;
import java.math.BigDecimal;

public class InventoryDto {

    // 목록 조회 응답용
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Integer ingredientId;
        private String ingredientName;
        private String baseUnit;
        private BigDecimal stockQty;
    }

    // [중요] 신규 등록(Insert) 요청용
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String ingredientName;
        private BigDecimal stockQty;
        private String baseUnit;
    }

    // 기존 수량 증가용
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddRequest {
        private BigDecimal quantity;
    }

    // 기존 수량 직접 수정용
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private BigDecimal stockQty;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UpdateResponse {
        private String message;
        private Integer ingredientId;
        private BigDecimal stockQty;
    }
}