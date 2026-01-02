package com.example.cust.repository;

import com.example.cust.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer> {

    /**
     * 1. 특정 고객의 모든 주문 내역을 최신순(Date 내림차순)으로 조회
     * OrderService의 getOrderHistoryList에서 사용됩니다.
     */
    List<Orders> findAllByCustomerIdOrderByOrderDateDesc(String customerId);

    /**
     * 2. 특정 상태의 주문 목록 조회
     */
    List<Orders> findByStatus(String status);

    /**
     * 3. 주문 상세 조회 (Fetch Join 활용)
     * 주문서 -> 아이템 -> 옵션까지 한 번에 쿼리하여 N+1 문제를 방지합니다.
     */
    @Query("SELECT o FROM Orders o " +
            "JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.orderOptions oo " +
            "WHERE o.orderId = :orderId")
    Optional<Orders> findDetailByIdWithItemsAndOptions(@Param("orderId") Integer orderId);
}