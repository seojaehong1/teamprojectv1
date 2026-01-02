package com.example.product.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_code")
    private Integer menuCode;

    @Column(name = "menu_name", nullable = false, unique = true)
    private String menuName;

    @Column(name = "allergy_ids")
    private String allergyIds; // "1, 2, 5"

    @Column(name = "category")
    private String category;

    @Column(name = "base_price", nullable = false)
    private Integer basePrice;

    @Column(name = "base_volume")
    private String baseVolume;

    @Column(name = "description")
    private String description;

    @Column(name = "create_time")
    private Integer createTime;

    @Column(name = "is_available")
    private Boolean isAvailable;
}
