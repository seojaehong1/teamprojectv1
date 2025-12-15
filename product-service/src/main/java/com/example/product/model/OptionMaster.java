package com.example.product.model;

import com.example.product.dto.OptionDto; // DTO 변환을 위해 임시로 추가
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
    private Integer id; // option_id (PK, auto_increment)에 매핑

    //그룹 이름 추가
    @Column(name = "option_group_name", length = 50, nullable = true) // DB 스키마: YES
    private String optionGroupName;

    @Column(name = "option_name", length = 100, nullable = false) // DB 스키마: NO
    private String optionName;

    @Column(name = "default_price", nullable = true) // 💡 nullable = false 대신 nullable = true로 잠시 변경
    private Integer defaultPrice; // int 타입 매핑

    // 변경 전 재료
    @Column(name = "from_material", length = 100, nullable = true) // DB 스키마: YES
    private String fromMaterial;

    // 변경 후 재료
    @Column(name = "to_material", length = 100, nullable = true) // DB 스키마: YES
    private String toMaterial;

    @Column(name = "quantity", precision = 8, scale = 2, nullable = true) // DB 스키마: YES
    private BigDecimal quantity;

    @Column(name = "unit", length = 10, nullable = true) // DB 스키마: YES
    private String unit;

    @Column(name = "process_method", length = 20, nullable = true) // DB 스키마: YES
    private String processMethod; // ENUM: '추가', '제거', '변경'


    public OptionDto toDto() {
        return OptionDto.builder()
                .optionId(this.id) // Integer
                .optionName(this.optionName) // String
                .optionPrice(this.defaultPrice)
                .optionGroupName(this.optionGroupName) // String
                .build();
    }
}