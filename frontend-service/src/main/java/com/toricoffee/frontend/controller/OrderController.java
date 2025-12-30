package com.toricoffee.frontend.controller;

import com.toricoffee.frontend.util.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/order") // 기본 경로: /order
public class OrderController {

    @Autowired
    private ApiClient apiClient;

    /**
     * 장바구니 페이지 접속
     * 실제 주소: GET /order/cart
     */
    @GetMapping("/cart") // 중복된 /order 제거
    public String cartPage(Model model) {
        try {
            // 백엔드 API 호출 (게이트웨이 주소 + /api/order/cart)
            List<?> cartItems = apiClient.get("/api/order/cart", List.class);

            model.addAttribute("cartItems", cartItems);
            log.info("장바구니 데이터 로드 성공: {} 건", cartItems != null ? cartItems.size() : 0);
        } catch (Exception e) {
            log.error("장바구니 데이터 가져오기 실패: {}", e.getMessage());
            model.addAttribute("cartItems", List.of()); // 에러 시 빈 리스트 전달
        }
        return "order/cart"; // templates/order/cart.html 호출
    }

    /**
     * 주문 결제 페이지
     * 실제 주소: GET /order/checkout
     */
    @GetMapping("/checkout")
    public String orderCheckout() {
        return "order/checkout";
    }

    /**
     * 주문 상세 페이지
     * 실제 주소: GET /order/detail
     */
    @GetMapping("/detail")
    public String orderDetail() {
        return "order/detail";
    }

    /**
     * 주문 이력 페이지
     * 실제 주소: GET /order/history
     */
    @GetMapping("/history")
    public String orderHistory() {
        return "order/history";
    }
}