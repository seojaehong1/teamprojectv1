package com.example.cust.service;

import com.example.cust.dto.*;
import com.example.cust.model.*;
import com.example.cust.repository.CartHeaderRepository;
import com.example.cust.repository.OrdersRepository;
import com.example.cust.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrdersRepository ordersRepository;
    private final CartDetailService cartDetailService;
    private final CartHeaderRepository cartHeaderRepository;
        private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    /**
     * 장바구니 데이터를 Orders, OrderItem, OrderOption 테이블에 저장하고 장바구니를 비웁니다.
     * (결제는 성공했다고 가정하며, 초기 상태는 PAYMENT_COMPLETED로 설정됩니다.)
     * @param customerId 주문을 요청한 고객 ID
     * @param customerName 주문을 요청한 고객 이름
     * @param requestMessage 주문 요청사항
     * @return 저장된 Orders 엔티티
     */
    @Transactional
    public Orders placeOrder(String customerId, String customerName, String requestMessage) {

        // 1. 장바구니 헤더 및 아이템 조회
        CartHeader cartHeader = cartDetailService.getCartHeaderByCustomerId(customerId);
        if (cartHeader == null || cartHeader.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어있습니다.");
        }

        List<CartItem> cartItems = cartHeader.getCartItems();
        int totalOrderAmount = cartItems.stream().mapToInt(CartItem::getTotalItemPrice).sum();

        // 3. Orders 엔티티 생성 및 기본 정보 설정
        Orders order = Orders.builder()
                .orderDate(LocalDateTime.now())
                .customerId(customerId)
                .customerName(customerName) // 고객 이름 저장
                .totalAmount(totalOrderAmount)
                .status(OrderStatus.PENDING)
                .request(requestMessage) // 이 부분이 추가되어야 DB에 저장됩니다!
                .build();

        // 4. CartItem을 OrderItem으로 변환하는 stream 부분
        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    OrderItem orderItem = OrderItem.builder()
                            .menuCode(cartItem.getMenuCode())
                            // 💡 [중요] 장바구니에 담긴 메뉴 이름을 주문 아이템에 넣어줍니다.
                            .menuName(cartItem.getMenuName())
                            .quantity(cartItem.getQuantity())
                            .priceAtOrder(cartItem.getUnitPrice())
                            .totalItemPrice(cartItem.getTotalItemPrice())
                            .order(order)
                            .build();

                    // 5. CartOption을 OrderOption으로 변환
                    List<OrderOption> orderOptions = cartItem.getCartOptions().stream()
                            .map(cartOption -> OrderOption.builder()
                                    .optionId(cartOption.getOptionId())
                                    // 💡 [중요] 옵션 이름도 함께 넣어주어야 합니다 (nullable=false인 경우)
                                    .optionName(cartOption.getOptionName())
                                    .optionPriceAtOrder(cartOption.getOptionPrice())
                                    .orderItem(orderItem)
                                    .build())
                            .collect(Collectors.toList());

                    orderItem.getOrderOptions().addAll(orderOptions);
                    return orderItem;
                })
                .collect(Collectors.toList());

        // 6. Orders 엔티티에 OrderItem 리스트 설정
        order.getOrderItems().addAll(orderItems);

        // 7. 주문 데이터 저장 (Cascade로 OrderItem, OrderOption도 저장됨)
        Orders savedOrder = ordersRepository.save(order);

        // 비동기 재고 차감 요청 전송 (RabbitMQ)
        try {
            OrderStockMessage msg = OrderStockMessage.builder()
                    .orderId(savedOrder.getOrderId())
                    .items(order.getOrderItems().stream()
                            .map(item -> OrderStockMessage.OrderItem.builder()
                                    .menuCode(item.getMenuCode())
                                    .quantity(item.getQuantity())
                                    .optionIds(item.getOrderOptions().stream().map(o -> o.getOptionId()).collect(java.util.stream.Collectors.toList()))
                                    .build())
                            .collect(java.util.stream.Collectors.toList()))
                    .build();

            rabbitTemplate.convertAndSend(RabbitConfig.ORDER_EXCHANGE, RabbitConfig.ORDER_PLACED_ROUTING_KEY, msg);
        } catch (Exception e) {
            // 메시지 전송 실패 시 로깅만 하고 주문 저장은 유지합니다.
            // TODO: DLQ(Dead Letter Queue)나 재시도 정책을 도입해야 합니다.
            log.error("Failed to publish order stock message: {}", e.getMessage(), e);
        }

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
                    List<OptionDto> optionDtos = item.getOrderOptions().stream()
                            .map(option -> OptionDto.builder()
                                    .optionId(option.getOptionId())
                                    .optionName(option.getOptionName()) // 💡 엔티티에 이름이 있다면 추가
                                    .optionPriceAtOrder(option.getOptionPriceAtOrder())
                                    .build())
                            .collect(Collectors.toList());

                    // OrderItem DTO 변환
                    return OrderItemDto.builder()
                            .menuCode(item.getMenuCode())
                            .menuName(item.getMenuName()) // 💡 엔티티에 menuName 필드 추가 필요
                            .quantity(item.getQuantity())
                            .priceAtOrder(item.getPriceAtOrder())
                            .totalItemPrice(item.getTotalItemPrice())
                            .orderOptions(optionDtos)
                            .build();
                })
                .collect(Collectors.toList());

        return OrderDetailDto.builder()
                .orderId(order.getOrderId())
                .orderDate(order.getOrderDate())
                .customerId(order.getCustomerId())
                .customerName(order.getCustomerName()) // 고객 이름 포함
                .request(order.getRequest())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().getDescription())
                .items(itemDtos)
                .build();
    }

    //주문 정보 가져오기
    @Transactional(readOnly = true)
    public List<Orders> getAllOrders() {
        return ordersRepository.findAll();
    }

    //주문 삭제
    @Transactional
    public void deleteAllOrders() {
        // 💡 [수정] deleteAllInBatch() 대신 deleteAll()을 사용합니다.
        // deleteAll()은 JPA 연관 관계(Cascade)를 따라 OrderItem, OrderOption을 먼저 삭제한 후 Orders를 삭제합니다.
        ordersRepository.deleteAll();
    }

    //주문 history 조회
    @Transactional(readOnly = true)
    public List<OrderHistoryDto> getOrderHistoryList(String customerId) {
        // 1. 고객의 전체 주문 목록 조회 (최신순)
        List<Orders> orders = ordersRepository.findAllByCustomerIdOrderByOrderDateDesc(customerId);

        // 2. 엔티티를 DTO로 변환
        return orders.stream()
                .map(order -> OrderHistoryDto.builder()
                        .orderId(order.getOrderId())
                        .orderDate(order.getOrderDate())
                        .totalAmount(order.getTotalAmount())
                        .status(order.getStatus().getDescription()) // Enum의 한글 설명값
                        .itemCount(order.getOrderItems().size())    // Set<OrderItem>의 크기
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 주문 history 조회 (기간별 필터링)
     * @param customerId 고객 ID
     * @param months 조회할 개월 수 (null이면 전체)
     */
    @Transactional(readOnly = true)
    public List<OrderHistoryDto> getOrderHistoryListByPeriod(String customerId, Integer months) {
        List<Orders> orders;

        if (months == null || months <= 0) {
            // 전체 조회
            orders = ordersRepository.findAllByCustomerIdOrderByOrderDateDesc(customerId);
        } else {
            // 기간별 조회
            LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
            orders = ordersRepository.findAllByCustomerIdAndOrderDateAfterOrderByOrderDateDesc(customerId, startDate);
        }

        // 엔티티를 DTO로 변환
        return orders.stream()
                .map(order -> OrderHistoryDto.builder()
                        .orderId(order.getOrderId())
                        .orderDate(order.getOrderDate())
                        .totalAmount(order.getTotalAmount())
                        .status(order.getStatus().getDescription())
                        .itemCount(order.getOrderItems().size())
                        .build())
                .collect(Collectors.toList());
    }
}