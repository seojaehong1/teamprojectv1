package com.example.product.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Integer orderItemId;

    // 연관 관계: OrderItem(N) <-> Orders(1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    @Column(name = "menu_code", length = 10, nullable = false)
    private String menuCode; // Menu Entity는 Product Module에 있으므로 String으로 유지

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price_at_order", nullable = false)
    private Integer priceAtOrder; // 주문 시점의 메뉴 항목 기본가

    @Column(name = "total_item_price", nullable = false)
    private Integer totalItemPrice; // 옵션 포함 항목 최종 가격

    // 연관 관계: OrderItem(1) <-> OrderOption(N)
    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderOption> orderOptions = new ArrayList<>();
}