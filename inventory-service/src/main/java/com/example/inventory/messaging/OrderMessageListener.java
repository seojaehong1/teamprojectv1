package com.example.inventory.messaging;

import com.example.inventory.dto.OrderStockRequestDto;
import com.example.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMessageListener {

    private final InventoryService inventoryService;

    @RabbitListener(queues = "inventory.order.queue")
    public void handleOrderPlaced(OrderStockRequestDto request) {
        // 비동기 처리: 주문이 들어오면 재고 차감 로직 호출
        inventoryService.processOrderStock(request);
    }
}
