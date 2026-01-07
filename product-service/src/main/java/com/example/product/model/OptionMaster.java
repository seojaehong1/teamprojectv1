package com.example.product.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "option_master")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Integer optionId;

    @Column(name = "option_group_name", nullable = false)
    private String optionGroupName;

    @Column(name = "option_name", nullable = false)
    private String optionName;

    @Column(name = "default_price", nullable = false)
    private BigDecimal defaultPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_material_id")
    private MaterialMaster fromMaterial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_material_id")
    private MaterialMaster toMaterial;

    @Column(name = "quantity", nullable = false)
    private Double quantity;

    @Column(name = "unit", nullable = false)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_method", nullable = false)
    private ProcessMethod processMethod;

    public enum ProcessMethod {
        추가, 제거, 변경
    }
}
