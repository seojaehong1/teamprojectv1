package com.example.cust.controller;

import com.example.cust.dto.OptionDto;
import com.example.cust.dto.ProductItemDto;
import com.example.cust.model.CartHeader;
import com.example.cust.model.CartItem;
import com.example.cust.model.Orders;
import com.example.cust.repository.OrdersRepository;
import com.example.cust.service.CartDetailService;
import com.example.cust.service.MakeCart;
import com.example.cust.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller; // 뷰를 반환하므로 @Controller 유지
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final MakeCart makeCartService;
    private final CartDetailService cartDetailService;
    private final OrderService orderService;

    @GetMapping("")
    public String home(Model model) {

        final int customerId = 1;

        // 1. 임시 데이터 생성: 상품 1 (옵션 포함)
        List<OptionDto> selectedOptions1 = Arrays.asList(
                OptionDto.builder().optionId(2).optionName("샷추가(+600)").optionPrice(600).optionGroupName("샷선택").build(),
                OptionDto.builder().optionId(6).optionName("바닐라시럽추가(+500)").optionPrice(500).optionGroupName("당도선택").build()
        );
        ProductItemDto productItem1 = ProductItemDto.builder()
                .customerId(1L).menuCode("cof-001").menuName("아메리카노").quantity(1).unitPrice(2500)
                .totalAmount(3600).options(selectedOptions1)
                .build();

        // 2. 임시 데이터 생성: 상품 2 (옵션 없음)
        ProductItemDto productItem2 = ProductItemDto.builder()
                .customerId(1L).menuCode("ade-001").menuName("청포도에이드").quantity(2).unitPrice(3500)
                .totalAmount(7000).options(List.of())
                .build();

        // 3. 두 항목을 리스트로 묶습니다.
        // 💡 단일 항목을 테스트할 때도 List.of(productItem1) 형태로 사용하면 됩니다.  -- 이건 테스트 데이터 아닐 시 구현 예정
        List<ProductItemDto> itemsToSave = Arrays.asList(productItem1, productItem2);

        try {
            // 4. CartHeader 조회/생성
            CartHeader cartHeader = makeCartService.getOrCreateCartHeader(customerId);

            // 5. List를 받는 서비스 메서드만 호출합니다.
            List<CartItem> savedCartItems = cartDetailService.addItemsToCart(cartHeader, itemsToSave);

            // 6. 저장된 항목들을 뷰에 전달
            model.addAttribute("cartItems", savedCartItems);

            // 7. 메시지 업데이트
            model.addAttribute("message",
                    String.format("장바구니 ID %d에 총 %d개 항목이 성공적으로 저장되었습니다.",
                            cartHeader.getCartId(), savedCartItems.size())
            );

        } catch (Exception e) {
            System.err.println("장바구니 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "장바구니 저장 중 오류 발생: " + e.getMessage());
        }

        return "home";
    }

    /**
     * [실제 API] 장바구니에 상품을 추가하는 REST API 엔드포인트입니다.
     * @param productItems 클라이언트로부터 받은 상품/옵션 정보 리스트
     */
    @PostMapping("/add")
    public ResponseEntity<String> addItemsToCart(@RequestBody List<ProductItemDto> productItems) {

        final int customerId = 1; // 임시: 실제로는 인증/세션 정보에서 추출해야 합니다.

        if (productItems == null || productItems.isEmpty()) {
            return new ResponseEntity<>("추가할 상품 정보가 없습니다.", HttpStatus.BAD_REQUEST);
        }

        try {
            // 1. 고객 ID를 기반으로 CartHeader를 조회하거나 새로 생성합니다.
            CartHeader cartHeader = makeCartService.getOrCreateCartHeader(customerId);

            if (cartHeader == null) {
                return new ResponseEntity<>("장바구니 헤더 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // 2. CartDetailService를 호출하여 CartItem과 CartOption을 DB에 저장합니다.
            List<CartItem> savedItems = cartDetailService.addItemsToCart(cartHeader, productItems);

            Integer cartId = cartHeader.getCartId();

            String responseMessage = String.format(
                    "장바구니 (ID: %d)에 상품 %d개와 %d개의 옵션이 성공적으로 저장되었습니다.",
                    cartId,
                    savedItems.size(),
                    savedItems.stream().mapToInt(item -> item.getCartOptions().size()).sum()
            );

            return new ResponseEntity<>(responseMessage, HttpStatus.OK);

        } catch (Exception e) {
            System.err.println("장바구니 추가 중 오류 발생: " + e.getMessage());
            return new ResponseEntity<>("장바구니 처리 중 서버 오류 발생: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/place")
    public ResponseEntity<String> placeOrder() {
        // 실제로는 인증/세션에서 추출해야 하지만, 테스트를 위해 1L로 고정
        final Integer customerId = 1;

        try {
            Orders savedOrder = orderService.placeOrder(customerId);

            String responseMessage = String.format(
                    "주문이 성공적으로 완료되었습니다. 주문 ID: %d, 총 결제 금액: %s원",
                    savedOrder.getOrderId(),
                    String.format("%,d", savedOrder.getTotalAmount())
            );

            // 주문 완료 후 주문 상세 DTO를 반환할 수도 있지만, 우선은 메시지 반환
            return new ResponseEntity<>(responseMessage, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("주문 처리 중 오류 발생: " + e.getMessage());
            return new ResponseEntity<>("주문 처리 중 서버 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}