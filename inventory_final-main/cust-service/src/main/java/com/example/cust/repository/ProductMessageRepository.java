package com.example.cust.repository;

import com.example.cust.model.ProductMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMessageRepository extends JpaRepository<ProductMessage, Long> {
    boolean existsByMessageId(String messageId);
}


