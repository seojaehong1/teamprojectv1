package com.example.product.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "recipe")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_detail_id")
    private Integer recipeDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_code", nullable = false)
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private MaterialMaster materialMaster;

    @Column(name = "required_quantity", nullable = false, precision = 8, scale = 2)
    private BigDecimal requiredQuantity; // 소요량

    @Column(name = "unit", nullable = false)
    private String unit;
}