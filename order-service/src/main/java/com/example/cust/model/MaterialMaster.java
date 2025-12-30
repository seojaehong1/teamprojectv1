package com.example.cust.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "material_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ingredient_id")
    private Integer ingredientId;

    @Column(name = "ingredient_name", length = 100, nullable = false, unique = true)
    private String ingredientName;

    @Column(name = "base_unit", length = 10, nullable = false)
    private String baseUnit; // 기본 재고 관리 단위

    @Column(name = "stock_qty", precision = 10, scale = 2)
    private BigDecimal stockQty; // 현재 재고량 (DECIMAL -> Double/BigDecimal)
}