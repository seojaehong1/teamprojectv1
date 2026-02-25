package com.example.product.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_option")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_code", nullable = false)
    private Menu menu;

    @Column(name = "option_group_name", nullable = false)
    private String optionGroupName;
}
