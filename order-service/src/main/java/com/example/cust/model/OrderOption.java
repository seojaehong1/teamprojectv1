package com.example.cust.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_option_id")
    private Integer orderOptionId;

    // 연관 관계: OrderOption(N) <-> OrderItem(1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name = "option_id", nullable = false)
    private Integer optionId; // OptionMaster Entity는 Product Module에 있으므로 Integer로 유지

    @Column(name = "option_price_at_order", nullable = false)
    private Integer optionPriceAtOrder; // 주문 시점의 옵션 가격
}