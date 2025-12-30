package com.example.cust.service;

import com.example.cust.config.RabbitConfig;
import com.example.cust.model.ProductMessage;
import com.example.cust.repository.ProductMessageRepository;
import com.google.gson.Gson;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ProductMessageConsumer {

    private final ProductMessageRepository repository;
    private final Gson gson = new Gson();

    public ProductMessageConsumer(ProductMessageRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME2)
    public void receiveMessage(String jsonMessage) {
        ProductMessage message = gson.fromJson(jsonMessage, ProductMessage.class);

        // ✅ messageId 중복 방지
        if (repository.existsByMessageId(message.getMessageId())) {
            System.out.println("⚠️ Duplicate message skipped: " + message.getMessageId());
            return; // 이미 저장된 메시지면 무시
        }

        repository.save(message);
        System.out.println("✅ Saved message: " + message.getTitle());
    }
}

