package com.example.cust.repository;

import com.example.cust.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Integer> {
    // 특정 고객의 모든 주문 내역 조회
    List<Orders> findByCustomerId(Integer customerId);

    // 특정 상태(PAID, PENDING 등)의 주문 목록 조회
    List<Orders> findByStatus(String status);

    @Query("SELECT o FROM Orders o " +
            "JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.orderOptions oo " +
            "WHERE o.orderId = :orderId")
    Optional<Orders> findDetailByIdWithItemsAndOptions(@Param("orderId") Integer orderId);
}