package com.example.inventory.service;

import com.example.inventory.config.RabbitConfig;
import com.example.inventory.dto.InventoryResultDto;
import com.example.inventory.dto.OrderStockRequestDto;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryMessageConsumer {

    private final InventoryService inventoryService;
    private final Gson gson = new Gson();

    @RabbitListener(queues = RabbitConfig.INVENTORY_QUEUE)
    public void receiveInventoryMessage(String jsonMessage) {
        try {
            log.info("재고 차감 메시지 수신: {}", jsonMessage);
            
            OrderStockRequestDto request = gson.fromJson(jsonMessage, OrderStockRequestDto.class);
            
            InventoryResultDto result = inventoryService.processOrderStock(request);
            
            if (result.isSucess()) {
                log.info("재고 차감 성공 - 주문 ID: {}, 메시지: {}", request.getOrderId(), result.getMessage());
            } else {
                log.error("재고 차감 실패 - 주문 ID: {}, 메시지: {}, 부족한 항목: {}", 
                    request.getOrderId(), result.getMessage(), result.getInsufficientItems());
            }
        } catch (Exception e) {
            log.error("재고 차감 메시지 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}

