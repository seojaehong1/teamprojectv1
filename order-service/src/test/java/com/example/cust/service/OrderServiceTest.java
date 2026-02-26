package com.example.cust.service;

import com.example.cust.config.RabbitConfig;
import com.example.cust.model.*;
import com.example.cust.repository.CartHeaderRepository;
import com.example.cust.repository.OrdersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * OrderService 단위 테스트
 * 핵심 비즈니스 로직인 주문 생성 및 조회 기능을 테스트합니다.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private CartDetailService cartDetailService;

    @Mock
    private CartHeaderRepository cartHeaderRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderService orderService;

    private CartHeader testCartHeader;
    private CartItem testCartItem;
    private Orders testOrder;

    @BeforeEach
    void setUp() {
        // 테스트용 CartOption 생성
        CartOption cartOption = CartOption.builder()
                .optionId(1)
                .optionName("샷 추가")
                .optionPrice(500)
                .build();

        // 테스트용 CartItem 생성
        testCartItem = CartItem.builder()
                .cartItemId(1)
                .menuCode("COFFEE001")
                .menuName("아메리카노")
                .quantity(2)
                .unitPrice(4500)
                .totalItemPrice(10000)
                .cartOptions(new ArrayList<>(List.of(cartOption)))
                .build();

        // 테스트용 CartHeader 생성
        testCartHeader = CartHeader.builder()
                .cartId(1)
                .customerId("testUser")
                .createdAt(LocalDateTime.now())
                .cartItems(new ArrayList<>(List.of(testCartItem)))
                .build();

        // 테스트용 Orders 생성
        testOrder = Orders.builder()
                .orderId(1)
                .customerId("testUser")
                .customerName("테스트유저")
                .orderDate(LocalDateTime.now())
                .totalAmount(10000)
                .status(OrderStatus.PENDING)
                .request("얼음 많이 주세요")
                .build();
    }

    @Test
    @DisplayName("주문 생성 성공 - 장바구니 아이템이 주문으로 변환되어야 한다")
    void placeOrder_Success() {
        // given
        String customerId = "testUser";
        String customerName = "테스트유저";
        String requestMessage = "얼음 많이 주세요";

        when(cartDetailService.getCartHeaderByCustomerId(customerId)).thenReturn(testCartHeader);
        when(ordersRepository.save(any(Orders.class))).thenAnswer(invocation -> {
            Orders order = invocation.getArgument(0);
            order.setOrderId(1);
            return order;
        });
        doNothing().when(cartHeaderRepository).delete(any(CartHeader.class));

        // when
        Orders result = orderService.placeOrder(customerId, customerName, requestMessage);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        assertThat(result.getCustomerName()).isEqualTo(customerName);
        assertThat(result.getRequest()).isEqualTo(requestMessage);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getTotalAmount()).isEqualTo(10000);
        assertThat(result.getOrderItems()).hasSize(1);

        // RabbitMQ 메시지 전송 확인
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConfig.ORDER_EXCHANGE),
                eq(RabbitConfig.ORDER_PLACED_ROUTING_KEY),
                any()
        );

        // 장바구니 삭제 확인
        verify(cartHeaderRepository).delete(testCartHeader);
    }

    @Test
    @DisplayName("주문 생성 실패 - 빈 장바구니일 경우 예외 발생")
    void placeOrder_EmptyCart_ThrowsException() {
        // given
        String customerId = "testUser";
        CartHeader emptyCartHeader = CartHeader.builder()
                .cartId(1)
                .customerId(customerId)
                .cartItems(new ArrayList<>())
                .build();

        when(cartDetailService.getCartHeaderByCustomerId(customerId)).thenReturn(emptyCartHeader);

        // when & then
        assertThatThrownBy(() -> orderService.placeOrder(customerId, "테스트", "요청사항"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니가 비어있습니다.");

        // 주문이 저장되지 않았는지 확인
        verify(ordersRepository, never()).save(any());
    }

    @Test
    @DisplayName("주문 생성 실패 - 장바구니가 없을 경우 예외 발생")
    void placeOrder_NoCart_ThrowsException() {
        // given
        String customerId = "testUser";
        when(cartDetailService.getCartHeaderByCustomerId(customerId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> orderService.placeOrder(customerId, "테스트", "요청사항"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니가 비어있습니다.");
    }

    @Test
    @DisplayName("주문 상세 조회 성공")
    void getOrderDetail_Success() {
        // given
        Integer orderId = 1;

        OrderOption orderOption = OrderOption.builder()
                .optionId(1)
                .optionName("샷 추가")
                .optionPriceAtOrder(500)
                .build();

        OrderItem orderItem = OrderItem.builder()
                .orderItemId(1)
                .menuCode("COFFEE001")
                .menuName("아메리카노")
                .quantity(2)
                .priceAtOrder(4500)
                .totalItemPrice(10000)
                .orderOptions(new ArrayList<>(List.of(orderOption)))
                .build();

        testOrder.setOrderItems(new ArrayList<>(List.of(orderItem)));

        when(ordersRepository.findDetailByIdWithItemsAndOptions(orderId))
                .thenReturn(Optional.of(testOrder));

        // when
        var result = orderService.getOrderDetail(orderId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getCustomerId()).isEqualTo("testUser");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getMenuName()).isEqualTo("아메리카노");
    }

    @Test
    @DisplayName("주문 상세 조회 실패 - 존재하지 않는 주문")
    void getOrderDetail_NotFound_ThrowsException() {
        // given
        Integer orderId = 999;
        when(ordersRepository.findDetailByIdWithItemsAndOptions(orderId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrderDetail(orderId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("주문 ID를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("RabbitMQ 메시지 전송 실패해도 주문은 저장되어야 한다")
    void placeOrder_RabbitMQFails_OrderStillSaved() {
        // given
        String customerId = "testUser";
        when(cartDetailService.getCartHeaderByCustomerId(customerId)).thenReturn(testCartHeader);
        when(ordersRepository.save(any(Orders.class))).thenAnswer(invocation -> {
            Orders order = invocation.getArgument(0);
            order.setOrderId(1);
            return order;
        });

        // RabbitMQ 전송 시 예외 발생
        doThrow(new RuntimeException("RabbitMQ connection failed"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any());

        // when
        Orders result = orderService.placeOrder(customerId, "테스트유저", "요청사항");

        // then - 주문은 여전히 저장됨
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1);
        verify(ordersRepository).save(any(Orders.class));
    }
}