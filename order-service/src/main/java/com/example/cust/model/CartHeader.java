package com.example.cust.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart_header")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Integer cartId;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 💡 [수정] mappedBy 제거 후 @JoinColumn 추가 (단방향)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cart_id")
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();
}