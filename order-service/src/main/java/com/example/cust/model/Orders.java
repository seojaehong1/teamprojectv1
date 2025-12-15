package com.example.cust.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "orders") // SQL 예약어와 겹칠 수 있으므로 @Table 명시
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "customer_id")
    private Integer customerId; // 고객 테이블의 FK

    @Enumerated(EnumType.STRING) // 💡 [수정] Enum 타입으로 변경
    @Column(name = "status", length = 20)
    private OrderStatus status;

    // 연관 관계: Orders(1) <-> OrderItem(N)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<OrderItem> orderItems = new HashSet<>(); // 💡 [수정] List -> Set, ArrayList -> HashSet
}