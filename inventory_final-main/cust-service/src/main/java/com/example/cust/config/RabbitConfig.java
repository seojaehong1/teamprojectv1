package com.example.cust.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String QUEUE_NAME = "message-queue";
    public static final String QUEUE_NAME2 = "product-queue";

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, false);
    }

    @Bean
    public Queue queue2() {
        return new Queue(QUEUE_NAME2, true);
    }
}

