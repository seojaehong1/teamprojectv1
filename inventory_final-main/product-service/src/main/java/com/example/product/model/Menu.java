package com.example.product.model;

import jakarta.persistence.*;
import lombok.*;

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
    @Column(name = "menu_code")
    private String menuCode;

    @Column(name = "menu_name")
    private String menuName;

    private String category;

    @Column(name = "base_price")
    private Integer basePrice;

    @Column(name = "base_volume")
    private String baseVolume;

    @Column(name = "allergy_info")
    private String allergyInfo;

    private String description;

    @Column(name = "create_time")
    private Integer createTime;
}
