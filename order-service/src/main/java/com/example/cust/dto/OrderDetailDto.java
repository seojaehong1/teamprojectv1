package com.example.cust.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDto {
    private Integer orderId;
    private LocalDateTime orderDate;
    private Integer customerId;
    private Integer totalAmount; // 전체 주문 총 금액
    private String status; // 주문 상태 (Enum의 description)
    private List<OrderItemDto> items; // 주문 항목 리스트
}