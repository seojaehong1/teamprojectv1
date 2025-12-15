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

    // Menu 엔티티의 ID를 참조 (외래 키)
    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    @Column(name = "calories", precision = 8, scale = 2)
    private BigDecimal calories; // 칼로리

    @Column(name = "sugar", precision = 8, scale = 2)
    private BigDecimal sugar; // 당류

    @Column(name = "protein", precision = 8, scale = 2)
    private BigDecimal protein; // 단백질

    // ... 기타 영양 정보 (지방, 나트륨 등)
}