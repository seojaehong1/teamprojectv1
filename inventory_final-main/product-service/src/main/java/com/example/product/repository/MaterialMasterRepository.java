package com.example.product.repository;

import com.example.product.model.MaterialMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaterialMasterRepository extends JpaRepository<MaterialMaster, Integer> {
    Optional<MaterialMaster> findByIngredientName(String ingredientName);
}