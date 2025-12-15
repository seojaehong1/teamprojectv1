package com.example.cust.repository;

import com.example.cust.model.CartHeader;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialMasterRepository extends JpaRepository<CartHeader, Long> {
}
