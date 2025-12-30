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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_code", length = 10)
    private Long menuCode; // PK

    @Column(name = "menu_name", length = 100, nullable = false)
    private String menuName;

    @Column(name = "allergy_ids")
    private String allergyIds;

    @Column(name = "base_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal basePrice; // 기본 가격

    @Column(name = "category", length = 50)
    private String category; // 카테고리 (COFFEE, TEA 등)

    @Column(name = "base_volume", length = 50)
    private String baseVolume; // 표기된 용량 (예: "L (710ml)")

    @Column(name = "description", length = 1000)
    private String description; // 메뉴 설명

    @Column(name = "create_time")
    private Integer createTime; // 제작 시간

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = true;

}

