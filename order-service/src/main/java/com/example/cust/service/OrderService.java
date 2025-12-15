package com.example.cust.service;

import com.example.cust.dto.OrderDetailDto;
import com.example.cust.dto.OrderItemDto;
import com.example.cust.dto.OrderOptionDto;
import com.example.cust.model.*;
import com.example.cust.repository.CartHeaderRepository;
import com.example.cust.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrdersRepository ordersRepository;
    private final CartDetailService cartDetailService;
    private final CartHeaderRepository cartHeaderRepository;

    /**
     * 장바구니 데이터를 Orders, OrderItem, OrderOption 테이블에 저장하고 장바구니를 비웁니다.
     * (결제는 성공했다고 가정하며, 초기 상태는 PAYMENT_COMPLETED로 설정됩니다.)
     * @param customerId 주문을 요청한 고객 ID
     * @return 저장된 Orders 엔티티
     */
    @Transactional
    public Orders placeOrder(Integer customerId) {

        // 1. 장바구니 헤더 및 아이템 조회
        CartHeader cartHeader = cartDetailService.getCartHeaderByCustomerId(customerId);
        if (cartHeader == null || cartHeader.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어있거나 찾을 수 없습니다.");
        }

        List<CartItem> cartItems = cartHeader.getCartItems();

        // 2. 주문 총 금액 계산 (CartItem의 계산된 getTotalItemPrice() 총합 사용)
        int totalOrderAmount = cartItems.stream()
                .mapToInt(CartItem::getTotalItemPrice)
                .sum();

        // 3. Orders 엔티티 생성 및 기본 정보 설정
        Orders order = Orders.builder()
                .orderDate(LocalDateTime.now())
                .customerId(customerId)
                .totalAmount(totalOrderAmount)
                .status(OrderStatus.PAYMENT_COMPLETED)
                .build();

        // 4. CartItem을 OrderItem으로 변환
        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    OrderItem orderItem = OrderItem.builder()
                            .menuCode(cartItem.getMenuCode())
                            .quantity(cartItem.getQuantity())
                            .priceAtOrder(cartItem.getUnitPrice())
                            .totalItemPrice(cartItem.getTotalItemPrice()) // 💡 계산된 Getter 사용
                            .order(order)
                            .build();

                    // 5. CartOption을 OrderOption으로 변환
                    List<OrderOption> orderOptions = cartItem.getCartOptions().stream()
                            .map(cartOption -> OrderOption.builder()
                                    .optionId(cartOption.getOptionId())
                                    .optionPriceAtOrder(cartOption.getOptionPrice()) // 💡 getOptionPrice() 사용
                                    .orderItem(orderItem)
                                    .build())
                            .collect(Collectors.toList());

                    // OrderItem에 OrderOption 리스트 설정
                    orderItem.getOrderOptions().addAll(orderOptions);
                    return orderItem;
                })
                .collect(Collectors.toList());

        // 6. Orders 엔티티에 OrderItem 리스트 설정
        order.getOrderItems().addAll(orderItems);

        // 7. 주문 데이터 저장 (Cascade로 OrderItem, OrderOption도 저장됨)
        Orders savedOrder = ordersRepository.save(order);

        // 8. 장바구니 비우기 (CartHeader 삭제)
        // 💡 [수정] 주문 완료 후, 해당 CartHeader를 삭제합니다.
        // 엔티티에 설정된 cascade 또는 orphanRemoval = true 설정에 따라 CartItem/CartOption도 함께 삭제됩니다.
        cartHeaderRepository.delete(cartHeader);

        return savedOrder;
    }

    // --- 주문 조회 및 DTO 변환 로직 ---

    /**
     * 주문 ID로 상세 정보를 조회하고 DTO로 변환합니다.
     * @param orderId 조회할 주문 ID
     * @return OrderDetailDto
     */
    @Transactional(readOnly = true)
    public OrderDetailDto getOrderDetail(Integer orderId) {

        // OrdersRepository에 findDetailByIdWithItemsAndOptions 쿼리가 정의되어 있어야 합니다.
        Orders order = ordersRepository.findDetailByIdWithItemsAndOptions(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 ID를 찾을 수 없습니다: " + orderId));

        return toDetailDto(order);
    }

    /**
     * Orders 엔티티를 OrderDetailDto로 변환하는 내부 메서드
     */
    private OrderDetailDto toDetailDto(Orders order) {

        List<OrderItemDto> itemDtos = order.getOrderItems().stream()
                .map(item -> {
                    // OrderOption DTO 변환
                    List<OrderOptionDto> optionDtos = item.getOrderOptions().stream()
                            .map(option -> OrderOptionDto.builder()
                                    .optionId(option.getOptionId())
                                    .optionPriceAtOrder(option.getOptionPriceAtOrder())
                                    // 필요한 경우 optionName 등을 추가
                                    .build())
                            .collect(Collectors.toList());

                    // OrderItem DTO 변환
                    return OrderItemDto.builder()
                            .menuCode(item.getMenuCode())
                            .quantity(item.getQuantity())
                            .priceAtOrder(item.getPriceAtOrder())
                            .totalItemPrice(item.getTotalItemPrice())
                            .orderOptions(optionDtos)
                            .build();
                })
                .collect(Collectors.toList());

        // OrderDetail DTO 변환
        return OrderDetailDto.builder()
                .orderId(order.getOrderId())
                .orderDate(order.getOrderDate())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().getDescription()) // Enum의 설명(예: "결제 완료") 사용
                .items(itemDtos)
                .build();
    }

    //주문 정보 가져오기
    @Transactional(readOnly = true)
    public List<Orders> getAllOrders() {
        // 실제 운영 환경에서는 페이징 처리가 필수입니다.
        return ordersRepository.findAll();
    }

    //주문 삭제
    @Transactional
    public void deleteAllOrders() {
        // 💡 [수정] deleteAllInBatch() 대신 deleteAll()을 사용합니다.
        // deleteAll()은 JPA 연관 관계(Cascade)를 따라 OrderItem, OrderOption을 먼저 삭제한 후 Orders를 삭제합니다.
        ordersRepository.deleteAll();
    }
}