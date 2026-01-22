package com.example.inventory.messaging;

import com.example.inventory.dto.InventoryResultDto;
import com.example.inventory.dto.OrderStockRequestDto;
import com.example.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageListener {

    private final InventoryService inventoryService;

    @RabbitListener(queues = "inventory.order.queue")
    public void handleOrderPlaced(OrderStockRequestDto request) {
        log.info("[재고] 주문 메시지 수신 - orderId: {}, items: {}", request.getOrderId(), request.getItems());
        // 비동기 처리: 주문이 들어오면 재고 차감 로직 호출
        InventoryResultDto result = inventoryService.processOrderStock(request);
        log.info("[재고] 처리 결과 - success: {}, message: {}, insufficient: {}", result.isSucess(), result.getMessage(), result.getInsufficientItems());
    }
}
