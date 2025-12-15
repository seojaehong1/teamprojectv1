package com.example.product.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "option_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Integer optionId;

    @Column(name = "option_group_name")
    private String optionGroupName;

    @Column(name = "option_name")
    private String optionName;

    @Column(name = "default_price", nullable = false)
    private Integer defaultPrice;

    @Column(name = "changing_material")
    private String changingMaterial;

    private Double quantity;
    private String unit;

    @Column(name = "process_method")
    private String processMethod;
}
