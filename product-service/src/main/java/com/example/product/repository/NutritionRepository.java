package com.example.product.repository;

import com.example.product.model.Nutrition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NutritionRepository extends JpaRepository<Nutrition, Integer> {
    Optional<Nutrition> findByMenu_MenuCode(Integer menuCode);
    void deleteByMenu_MenuCode(Integer menuCode);
}
