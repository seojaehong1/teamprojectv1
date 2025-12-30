package com.example.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long id;          // menuCode
    private String name;        // menuName
    private String description; // category 또는 설명
    private BigDecimal price;   // basePrice
    private Integer stock;      // 재고 (기본값 0)
}

