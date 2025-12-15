package com.example.cust.controller;

import com.example.cust.dto.OrderDetailDto;
import com.example.cust.model.Orders;
import com.example.cust.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OrderService orderService;

    /**
     * [관리자] 모든 주문 목록 조회 (http://localhost:8002/admin)
     */
    @GetMapping
    public String listOrders(Model model) {
        List<Orders> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "admin/order-list"; // 💡 admin/order-list.html 템플릿 필요
    }

    /**
     * [관리자] 특정 주문 상세 정보 조회 (DTO 반환)
     */
    @GetMapping("/orders/{orderId}")
    public String orderDetail(@PathVariable Integer orderId, Model model) {
        try {
            OrderDetailDto detailDto = orderService.getOrderDetail(orderId);
            model.addAttribute("order", detailDto);
            return "admin/order-detail"; // 💡 admin/order-detail.html 템플릿 필요
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error/404";
        }
    }

    @PostMapping("/reset")
    public String resetOrders() {
        orderService.deleteAllOrders();
        // 삭제 후 목록 페이지로 리다이렉트
        return "redirect:/admin?message=주문+데이터가+성공적으로+초기화되었습니다.";
    }
}