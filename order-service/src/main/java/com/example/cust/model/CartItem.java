package com.example.cust.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Integer cartItemId;

    @Column(name = "menu_name", length = 50, nullable = false) // 💡 [추가] 메뉴 이름 필드
    private String menuName;

    // 연관 관계: CartItem(N) <-> CartHeader(1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private CartHeader cartHeader;

    @Column(name = "menu_code", length = 10, nullable = false)
    private String menuCode;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice; // 장바구니 담을 시점의 메뉴 기본 가격

    // 연관 관계: CartItem(1) <-> CartOption(N)
    @OneToMany(mappedBy = "cartItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartOption> cartOptions = new ArrayList<>();

    public void setCartOptions(List<CartOption> cartOptions) {
        this.cartOptions = cartOptions;
        // 💡 양방향 관계 설정 (핵심!)
        for (CartOption option : cartOptions) {
            option.setCartItem(this);
        }
    }

    public Integer getTotalItemPrice() {
        // 1. 단가 * 수량
        int basePrice = this.unitPrice * this.quantity;

        // 2. 옵션 가격 총합 계산 (옵션이 null이거나 비어있을 경우 0 처리)
        int optionPrice = 0;
        if (this.cartOptions != null && !this.cartOptions.isEmpty()) {
            // CartOption 엔티티의 optionPrice 필드 (기존 필드)를 사용한다고 가정
            optionPrice = this.cartOptions.stream()
                    .mapToInt(CartOption::getOptionPrice)
                    .sum();
        }

        return basePrice + optionPrice;
    }
}