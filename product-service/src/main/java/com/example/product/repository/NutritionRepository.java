package com.example.product.repository;

import com.example.product.model.Nutrition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NutritionRepository extends JpaRepository<Nutrition, Long> {
    Optional<Nutrition> findByMenu_MenuCode(Long menuCode);
    void deleteByMenu_MenuCode(Long menuCode);
}
