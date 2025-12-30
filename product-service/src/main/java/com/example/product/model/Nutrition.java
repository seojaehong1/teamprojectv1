package com.example.product.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "nutrition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nutrition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_code", nullable = false, length = 20)
    private String menuCode;

    @Column(precision = 8, scale = 2)
    private BigDecimal calories;

    @Column(precision = 8, scale = 2)
    private BigDecimal sodium;

    @Column(precision = 8, scale = 2)
    private BigDecimal carbs;

    @Column(precision = 8, scale = 2)
    private BigDecimal sugars;

    @Column(precision = 8, scale = 2)
    private BigDecimal protein;

    @Column(precision = 8, scale = 2)
    private BigDecimal fat;

    @Column(precision = 8, scale = 2)
    private BigDecimal saturatedFat;

    @Column(precision = 8, scale = 2)
    private BigDecimal caffeine;
}
