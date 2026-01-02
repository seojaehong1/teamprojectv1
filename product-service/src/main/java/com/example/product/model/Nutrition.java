package com.example.product.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "nutrition")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nutrition {

    @Id
    @Column(name = "menu_code")
    private Integer menuCode;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_code")
    private Menu menu;

    private BigDecimal calories;
    private BigDecimal sodium;
    private BigDecimal carbs;
    private BigDecimal sugars;
    private BigDecimal protein;
    private BigDecimal fat;

    @Column(name = "saturated_fat")
    private BigDecimal saturatedFat;

    private BigDecimal caffeine;
}
