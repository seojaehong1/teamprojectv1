package com.example.product.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_option") // 실제 테이블 이름에 따라 조정 가능
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Menu Entity의 PK (menu_code: String)를 저장
    @Column(name = "menu_code", length = 10, nullable = false)
    private String menuCode;

    //OptionMaster의 옵션 그룹 이름 (String)을 저장
    @Column(name = "option_group_name", length = 50, nullable = false)
    private String optionGroupName;

//    // 추가 필드: 해당 옵션이 필수인지 선택인지 등을 정의 가능 - 안쓸듯
//    @Column(name = "is_required")
//    private Boolean isRequired; 
}