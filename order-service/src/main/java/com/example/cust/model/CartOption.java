package com.example.cust.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_option_id")
    private Integer cartOptionId;

    @Column(name = "option_name", length = 50, nullable = false)
    private String optionName;

    // 💡 [삭제] private CartItem cartItem;
    // 이제 부모 엔티티를 직접 참조하지 않습니다.

    @Column(name = "option_id", nullable = false)
    private Integer optionId;

    @Column(name = "option_price", nullable = false)
    private Integer optionPrice;
}