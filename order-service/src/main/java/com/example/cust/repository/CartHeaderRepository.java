package com.example.cust.repository;

import com.example.cust.model.CartHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartHeaderRepository extends JpaRepository<CartHeader, Integer> {

    // 특정 고객 ID에 해당하는 활성 CartHeader를 찾습니다.
    Optional<CartHeader> findByCustomerId(Integer customerId);
}