package com.example.cust.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderHistoryDto {
    private Integer orderId;       // 주문 번호
    private LocalDateTime orderDate; // 주문 일시
    private Integer totalAmount;   // 총 주문 금액
    private String status;         // 주문 상태 (Description 값)
    private Integer itemCount;     // 주문 상품 종류 개수 (예: 아메리카노 외 1건 -> 2)
}