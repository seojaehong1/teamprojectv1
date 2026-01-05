package com.example.cust.controller;

import com.example.cust.dto.OptionDto;
import com.example.cust.dto.OrderHistoryDto;
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
import java.util.Map;

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
    public ResponseEntity<List<CartItem>> getCart() {
        final String customerId = "1";

        CartHeader cartHeader =
                makeCartService.getOrCreateCartHeader(customerId);

        return ResponseEntity.ok(cartHeader.getCartItems());
    }

    /**
     * 2. 장바구니 상품 수량 수정 API
     */
    @PutMapping("/cart/items/{cartItemId}")
    public ResponseEntity<?> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @RequestBody Map<String, Integer> request) {

        try {
            int quantity = request.get("quantity");
            // cartDetailService에 수량 변경 로직이 필요합니다.
            cartDetailService.updateQuantity(cartItemId, quantity);

            return ResponseEntity.ok(Map.of(
                    "message", "수량이 변경되었습니다.",
                    "cartItemId", cartItemId,
                    "quantity", quantity
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("수정 실패");
        }
    }

    /**
     * 3. 장바구니 상품 삭제 API
     */
    @DeleteMapping("/cart/items/{cartItemId}")
    public ResponseEntity<?> deleteCartItem(@PathVariable Long cartItemId) {
        try {
            // cartDetailService에 삭제 로직 필요
            cartDetailService.deleteItem(cartItemId);
            return ResponseEntity.ok(Map.of("message", "상품이 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제 실패");
        }
    }
    /**
     * 4. 장바구니 상품 개수 조회 API
     */
    @GetMapping("/cart/count")
    public ResponseEntity<?> getCartCount() {
        final String customerId = "1"; // 실무에선 토큰에서 추출
        try {
            CartHeader cartHeader = makeCartService.getOrCreateCartHeader(customerId);
            int count = cartHeader.getCartItems().size();
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("count", 0)); // 에러 시 0개 반환
        }
    }

    /**
     * 5. 장바구니 상품 추가 API (JSON 기반)
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
     * 7. 주문하기 API
     */
    @PostMapping("/place")
    public ResponseEntity<String> placeOrder(@RequestBody Map<String, String> requestBody) {
        try {
            // 프론트엔드에서 보낸 "request" 키값을 추출
            String requestMessage = requestBody.get("request");

            // 서비스 메서드에 요청사항 전달
            Orders savedOrder = orderService.placeOrder("1", requestMessage);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("주문 완료 ID: " + savedOrder.getOrderId());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("주문 실패: " + e.getMessage());
        }
    }

    /**
     * 8. 주문 상세 조회 API
     * @param orderId 조회할 주문 ID
     * @return 주문 상세 DTO (아이템 및 옵션 포함)
     */
    @GetMapping("/detail/{orderId}")
    public ResponseEntity<?> getOrderDetail(@PathVariable("orderId") Integer orderId) {
        log.info("주문 상세 조회 요청 - ID: {}", orderId);
        try {
            // orderService에서 데이터를 가져옵니다.
            var orderDetail = orderService.getOrderDetail(orderId);

            if (orderDetail == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("주문 내역이 없습니다.");
            }

            return ResponseEntity.ok(orderDetail);
        } catch (Exception e) {
            log.error("주문 상세 조회 중 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }

    /**
     * 9. 주문 내역 목록 조회
     * GET /api/order/history
     */
    @GetMapping("/history")
    public ResponseEntity<List<OrderHistoryDto>> getOrderHistory() {
        final String customerId = "1";
        log.info("주문 내역 DTO 조회 요청 - 고객: {}", customerId);
        try {
            List<OrderHistoryDto> historyList = orderService.getOrderHistoryList(customerId);
            return ResponseEntity.ok(historyList);
        } catch (Exception e) {
            log.error("주문 내역 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 6. 테스트 데이터 생성용 API (request 필드 제거됨)
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
     * 6. 테스트 데이터 생성용 API (request 필드 제거됨)
     */
    @GetMapping("/test-add2")
    public ResponseEntity<?> testAdd2() {
        //('청포도에이드', '에이드', 3500, '710mL', NULL, '산뜻한 청포도와 상쾌한 탄산의 달달한 조화가 인상적인 에이드.', 1, true),
        //('당도선택', '바닐라시럽추가(+500)', 500, NULL, '바닐라 시럽', 20.00, 'g', '추가'),
        final String customerId = "1";

        // request 설정 없이 데이터 생성
        ProductItemDto productItem1 = ProductItemDto.builder()
                .customerId(customerId).menuCode(16L).menuName("청포도에이드").quantity(2).unitPrice(3500)
                .totalAmount(1) // 2500 + 600
                .options(List.of(
                        OptionDto.builder().optionId(2).optionName("샷추가").optionPrice(600).optionGroupName("샷선택").build(),
                        OptionDto.builder().optionId(6).optionName("바닐라시럽추가(+500)").optionPrice(500).optionGroupName("당도선택").build()
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


}