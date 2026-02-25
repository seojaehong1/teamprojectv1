package com.example.cust.repository;

import com.example.cust.model.OrderOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<OrderOption, Long> {
}
