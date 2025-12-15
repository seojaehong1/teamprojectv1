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
    private Integer customerId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 연관 관계: CartHeader(1) <-> CartItem(N)
    @OneToMany(mappedBy = "cartHeader", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();
}