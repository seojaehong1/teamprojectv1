package com.example.product.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "status", length = 20)
    private String status;

    // 연관 관계: Orders(1) <-> OrderItem(N)
    // 주문이 삭제되면 항목도 같이 삭제 (Cascade)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();
}