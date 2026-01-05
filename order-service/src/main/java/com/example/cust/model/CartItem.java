package com.example.cust.model;

import com.example.cust.model.CartOption;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "menu_name", length = 50, nullable = false)
    private String menuName;

    // 💡 [삭제] private CartHeader cartHeader;
    // 이제 CartItem은 부모 정보를 가지지 않습니다.

    @Column(name = "menu_code",  nullable = false)
    private Long menuCode;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    // 💡 [수정] mappedBy 제거 후 @JoinColumn 추가 (단방향)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cart_item_id")
    @Builder.Default
    private List<CartOption> cartOptions = new ArrayList<>();

    // 💡 [수정] 편의 메서드에서 option.setCartItem(this) 삭제
    public void setCartOptions(List<CartOption> cartOptions) {
        this.cartOptions = cartOptions;
    }

    public Integer getTotalItemPrice() {
        // 1. 개별 옵션들의 가격 합계를 먼저 구합니다.
        int totalOptionPrice = 0;
        if (this.cartOptions != null && !this.cartOptions.isEmpty()) {
            totalOptionPrice = this.cartOptions.stream()
                    .mapToInt(CartOption::getOptionPrice)
                    .sum();
        }

        // 2. (메뉴 단가 + 옵션 합계)에 전체 수량을 곱합니다.
        return (this.unitPrice + totalOptionPrice) * this.quantity;
    }
}