package com.example.product.repository;

import com.example.product.model.Nutrition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionRepository extends JpaRepository<Nutrition, String> {
}