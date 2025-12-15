package com.example.inventory.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String INVENTORY_QUEUE = "inventory-queue";

    @Bean
    public Queue inventoryQueue() {
        return new Queue(INVENTORY_QUEUE, true); // durable = true
    }
}

