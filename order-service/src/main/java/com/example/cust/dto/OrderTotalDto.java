package com.example.cust.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderTotalDto {
    private long pendingCount;    // 결제 완료 건수
    private long preparingCount;  // 주문 접수 건수
    private long completedCount;  // 완료 건수
    private long totalRevenue;    // 전체 매출액 (모든 totalAmount의 합)
}
