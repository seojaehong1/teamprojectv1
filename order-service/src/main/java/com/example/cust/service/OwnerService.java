package com.example.cust.service;

import com.example.cust.dto.OptionDto;
import com.example.cust.dto.OrderDetailDto;
import com.example.cust.dto.OrderItemDto;
import com.example.cust.dto.OrderTotalDto;
import com.example.cust.model.OrderStatus;
import com.example.cust.model.Orders;
import com.example.cust.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerService {

    private final OrdersRepository orderRepository;

    public List<OrderDetailDto> getAllOrdersForOwner() {
        // 조건 없이 모든 주문 조회
        List<Orders> orders = orderRepository.findAll();

        return orders.stream().map(order -> {
            // 하위 아이템 및 옵션 변환 로직
            List<OrderItemDto> itemDtos = order.getOrderItems().stream()
                    .map(item -> {
                        List<OptionDto> optionDtos = item.getOrderOptions().stream()
                                .map(option -> OptionDto.builder()
                                        .optionId(option.getOptionId())
                                        .optionName(option.getOptionName())
                                        .optionPriceAtOrder(option.getOptionPriceAtOrder())
                                        .build())
                                .collect(Collectors.toList());

                        return OrderItemDto.builder()
                                .menuCode(item.getMenuCode())
                                .menuName(item.getMenuName())
                                .quantity(item.getQuantity())
                                .priceAtOrder(item.getPriceAtOrder())
                                .totalItemPrice(item.getTotalItemPrice())
                                .orderOptions(optionDtos)
                                .build();
                    })
                    .collect(Collectors.toList());

            // 최종 DTO 조립
            return OrderDetailDto.builder()
                    .orderId(order.getOrderId())
                    .orderDate(order.getOrderDate())
                    .customerId(order.getCustomerId())
                    .request(order.getRequest())
                    .totalAmount(order.getTotalAmount())
                    .status(order.getStatus() != null ? order.getStatus().getDescription() : "접수 대기")
                    .items(itemDtos)
                    .build();
        }).collect(Collectors.toList());
    }

    public OrderStatus fromDescription(String description) {
        for (OrderStatus os : OrderStatus.values()) {
            if (os.getDescription().equals(description)) {
                return os;
            }
        }
        return OrderStatus.PENDING; // 기본값
    }

    /**
     * 상태별 주문 목록 조회 지금은 뷰에서 처리중
     */
    public List<Orders> getOrdersByStatus(String status) {
        if (status == null || status.isEmpty() || "전체".equals(status)) {
            return orderRepository.findAll();
        }
        return orderRepository.findByStatus(status);
    }

    /**
     * 주문 데이터 전체 삭제 (초기화)
     */
    @Transactional
    public void resetAllOrders() {
        orderRepository.deleteAll();
    }


    // 주문 상태 변경
    @Transactional
    public void updateNextStatus(Integer orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 없습니다."));

        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.PREPARING); // 결제 완료 -> 주문 접수
        } else if (currentStatus == OrderStatus.PREPARING) {
            order.setStatus(OrderStatus.COMPLETED); // 주문 접수 -> 완료
        }
        // 더 이상 변경할 단계가 없으면 로직을 종료하거나 예외 처리
    }


    //통계 출력
    @Transactional(readOnly = true)
    public OrderTotalDto getOrderTotals() {
        List<Orders> allOrders = orderRepository.findAll();

        // 1. 상태별 카운트 계산
        long pendingCount = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .count();

        long preparingCount = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PREPARING)
                .count();

        long completedCount = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .count();

        // 2. 전체 매출 합계 계산 (totalAmount가 null인 경우 대비)
        long totalRevenue = allOrders.stream()
                .mapToLong(o -> o.getTotalAmount() != null ? o.getTotalAmount().longValue() : 0L)
                .sum();

        return OrderTotalDto.builder()
                .pendingCount(pendingCount)
                .preparingCount(preparingCount)
                .completedCount(completedCount)
                .totalRevenue(totalRevenue)
                .build();
    }
}