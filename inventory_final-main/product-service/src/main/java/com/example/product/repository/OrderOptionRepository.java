package com.example.product.repository;

import com.example.product.model.OrderOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderOptionRepository extends JpaRepository<OrderOption, Integer> {
    List<OrderOption> findByOrderItem_OrderItemId(Integer orderItemId);
}