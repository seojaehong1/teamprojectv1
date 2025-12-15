package com.example.cust.service;

import com.example.cust.config.RabbitConfig;
import com.example.cust.model.Orders;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryMessageProducer {

    private final RabbitTemplate rabbitTemplate;
    private final Gson gson = new Gson();

    /**
     * 주문 정보를 재고 차감 메시지로 변환하여 RabbitMQ에 발송
     */
    public void sendInventoryDeductionMessage(Orders order) {
        try {
            // OrderStockRequestDto 형태로 변환
            InventoryStockRequest request = new InventoryStockRequest();
            request.setOrderId(order.getOrderId());

            List<InventoryStockRequest.OrderItem> items = order.getOrderItems().stream()
                    .map(orderItem -> {
                        InventoryStockRequest.OrderItem item = new InventoryStockRequest.OrderItem();
                        item.setMenuCode(orderItem.getMenuCode());
                        item.setQuantity(orderItem.getQuantity());
                        
                        // OrderOption에서 optionId 리스트 추출
                        List<Integer> optionIds = orderItem.getOrderOptions().stream()
                                .map(option -> option.getOptionId())
                                .collect(Collectors.toList());
                        item.setOptionIds(optionIds);
                        
                        return item;
                    })
                    .collect(Collectors.toList());

            request.setItems(items);

            String jsonMessage = gson.toJson(request);
            rabbitTemplate.convertAndSend(RabbitConfig.INVENTORY_QUEUE, jsonMessage);
            
            log.info("재고 차감 메시지 발송 완료 - 주문 ID: {}", order.getOrderId());
        } catch (Exception e) {
            log.error("재고 차감 메시지 발송 실패 - 주문 ID: {}, 오류: {}", order.getOrderId(), e.getMessage(), e);
        }
    }

    /**
     * RabbitMQ로 전송할 재고 차감 요청 DTO
     * inventory-service의 OrderStockRequestDto와 동일한 구조
     */
    @lombok.Data
    public static class InventoryStockRequest {
        private Integer orderId;
        private List<OrderItem> items;

        @lombok.Data
        public static class OrderItem {
            private String menuCode;
            private Integer quantity;
            private List<Integer> optionIds;
        }
    }
}

