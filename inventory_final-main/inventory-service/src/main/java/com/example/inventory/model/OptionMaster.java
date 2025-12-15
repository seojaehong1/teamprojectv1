package com.example.inventory.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

//    @Column(name = "changing_material")
//    private String changingMaterial;

    // 변경 전 재료
    @Column(name = "from_material")
    private String fromMaterial;

    // 변경 후 재료
    @Column(name = "to_material")
    private String toMaterial;

    @Column(name = "quantity", precision = 8, scale = 2)
    private BigDecimal quantity; // 이거 Double이었는데 오류 때문에 제가 BigDecimal로 변경했어요 확인 요함

    @Column(name = "unit", length = 10)
    private String unit;

    @Column(name = "process_method")
    private String processMethod; // ENUM: '추가', '제거', '변경'
}
