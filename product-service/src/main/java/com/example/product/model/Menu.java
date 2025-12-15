package com.example.product.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "menu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {


    @Id
    @Column(name = "menu_code", length = 10)
    private String menuCode; // PK

    @Column(name = "menu_name", length = 100, nullable = false)
    private String menuName;

    @Column(name = "base_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal basePrice; // 기본 가격

    @Column(name = "category", length = 50)
    private String category; // 카테고리 (COFFEE, TEA 등)

    // 이 외에 이미지 경로, 설명 등을 추가할 수 있습니다.
}

