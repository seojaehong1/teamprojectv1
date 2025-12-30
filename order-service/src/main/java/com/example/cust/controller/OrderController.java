package com.example.cust.controller;

import com.example.cust.dto.OptionDto;
import com.example.cust.dto.ProductItemDto;
import com.example.cust.model.CartHeader;
import com.example.cust.model.CartItem;
import com.example.cust.model.Orders;
import com.example.cust.service.CartDetailService;
import com.example.cust.service.MakeCart;
import com.example.cust.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final MakeCart makeCartService;
    private final CartDetailService cartDetailService;
    private final OrderService orderService;

    /**
     * 1. 장바구니 조회 API
     */
    @GetMapping("/cart")
    public ResponseEntity<?> getCart() {
        final String customerId = "1";
        try {
            CartHeader cartHeader = makeCartService.getOrCreateCartHeader(customerId);
            return ResponseEntity.ok(cartHeader.getCartItems());
        } catch (Exception e) {
            log.error("장바구니 조회 오류: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("조회 실패");
        }
    }

    /**
     * 2. 장바구니 상품 추가 API (JSON 기반)
     */
    @PostMapping("/add")
    public ResponseEntity<String> addItemsToCart(@RequestBody List<ProductItemDto> productItems) {
        final String customerId = "1";
        if (productItems == null || productItems.isEmpty()) {
            return ResponseEntity.badRequest().body("상품 정보가 없습니다.");
        }
        try {
            CartHeader cartHeader = makeCartService.getOrCreateCartHeader(customerId);
            List<CartItem> savedItems = cartDetailService.addItemsToCart(cartHeader, productItems);
            return ResponseEntity.ok(String.format("장바구니(ID: %d)에 상품 %d개가 저장되었습니다.",
                    cartHeader.getCartId(), savedItems.size()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("처리 중 오류 발생");
        }
    }

    /**
     * 3. 테스트 데이터 생성용 API (request 필드 제거됨)
     */
    @GetMapping("/test-add")
    public ResponseEntity<?> testAdd() {
        final String customerId = "1";

        // request 설정 없이 데이터 생성
        ProductItemDto productItem1 = ProductItemDto.builder()
                .customerId(customerId).menuCode(1L).menuName("아메리카노").quantity(1).unitPrice(2500)
                .totalAmount(3100) // 2500 + 600
                .options(List.of(
                        OptionDto.builder().optionId(2).optionName("샷추가").optionPrice(600).optionGroupName("샷선택").build()
                ))
                .build();

        try {
            CartHeader cartHeader = makeCartService.getOrCreateCartHeader(customerId);
            List<CartItem> savedItems = cartDetailService.addItemsToCart(cartHeader, List.of(productItem1));
            return ResponseEntity.ok(savedItems);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    /**
     * 4. 주문하기 API
     */
    @PostMapping("/place")
    public ResponseEntity<String> placeOrder() {
        try {
            Orders savedOrder = orderService.placeOrder("1");
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("주문 완료 ID: " + savedOrder.getOrderId());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("주문 실패");
        }
    }
}