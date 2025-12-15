package com.example.product.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductItemDto {

    // 주문자 정보
    private Long customerId;

    // 메뉴 기본 정보
    private String menuCode;
    private Integer quantity;
    private Integer unitPrice; // 메뉴 기본 가격 (unit_price)

    // 가격 정보
    private Integer totalAmount; // 총 금액 (옵션+제품가격)

    // 옵션 정보 (수정된 OptionDto 리스트)
    private List<OptionDto> options;
}