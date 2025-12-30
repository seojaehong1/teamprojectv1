package com.example.product.model;

import jakarta.persistence.*;
import lombok.*;

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
    @Column(name = "menu_code")
    private String menuCode;

    @OneToOne
    @JoinColumn(name = "menu_code", insertable = false, updatable = false)
    private Menu menu;

    private Double calories;
    private Double sodium;
    private Double carbs;
    private Double sugars;
    private Double protein;
    private Double fat;

    @Column(name = "saturated_fat")
    private Double saturatedFat;

    private Double caffeine;
}
