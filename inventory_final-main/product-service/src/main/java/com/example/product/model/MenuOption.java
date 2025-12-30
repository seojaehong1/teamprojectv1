package com.example.product.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "menu_option",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"menu_code", "option_group_name"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_code", nullable = false, length = 10)
    private String menuCode;

    @Column(name = "option_group_name", nullable = false, length = 50)
    private String optionGroupName;
}
