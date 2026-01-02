package com.example.cust.controller;

import com.example.cust.dto.OrderDetailDto;
import com.example.cust.dto.OrderTotalDto;
import com.example.cust.model.Orders;
import com.example.cust.service.OwnerService; // 새로운 서비스 주입
import com.example.cust.service.OrderService; // 상세 정보 조회는 기존 서비스 활용 가능
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class AdminController {

    private final OwnerService ownerService;
    private final OrderService orderService;

    @GetMapping("/order")
    public ResponseEntity<List<OrderDetailDto>> getOrders() {
        log.info("모든 주문 내역 조회 요청");
        List<OrderDetailDto> dtoList = ownerService.getAllOrdersForOwner();
        return ResponseEntity.ok(dtoList);
    }

    /**
     * 주문 초기화
     * POST /api/owner/orders/reset
     */
    @PostMapping("/order/reset")
    public ResponseEntity<?> resetOrders() {
        ownerService.resetAllOrders();
        return ResponseEntity.ok(Map.of("message", "주문 데이터가 초기화되었습니다."));
    }

    // 상세 조회가 필요하다면 기존 orderService의 DTO 로직 호출
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Integer orderId) {
        return ResponseEntity.ok(orderService.getOrderDetail(orderId));
    }

    @PatchMapping("/order/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable Integer orderId) {
        ownerService.updateNextStatus(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/total")
    public ResponseEntity<OrderTotalDto> getStatistics() {
        log.info("점주 통계 데이터 조회 요청");
        OrderTotalDto stats = ownerService.getOrderTotals();
        return ResponseEntity.ok(stats);
    }
}