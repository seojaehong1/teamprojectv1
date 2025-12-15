package com.example.cust.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "recipe")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_detail_id")
    private Integer recipeDetailId;

    @Column(name = "menu_code", length = 10, nullable = false)
    private String menuCode; // Menu Entity는 Product Module에 있음

    // 연관 관계: Recipe(N) <-> MaterialMaster(1)
    // 재료명(String)을 FK로 사용하셨기 때문에 @ManyToOne 매핑을 위해 MaterialMaster 엔티티를 참조합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_name", referencedColumnName = "ingredient_name", nullable = false)
    private MaterialMaster materialMaster;

    @Column(name = "ingredient_category", length = 50)
    private String ingredientCategory;

    @Column(name = "required_quantity", precision = 8, scale = 2, nullable = false)
    private BigDecimal requiredQuantity; // 소요량

    @Column(name = "unit", length = 10, nullable = false)
    private String unit;
}