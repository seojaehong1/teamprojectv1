package com.example.cust.repository;

import com.example.cust.model.CartOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartOptionRepository extends JpaRepository<CartOption, Long> {
}
